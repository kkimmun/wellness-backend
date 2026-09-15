#!/bin/bash
set -e

# 패키지 업데이트
sudo apt update
sudo apt upgrade -y

# 기본 도구 설치
sudo apt install -y \
  apt-transport-https \
  ca-certificates \
  curl \
  software-properties-common

# Docker가 없는 경우 설치
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com -o get-docker.sh
  sudo sh get-docker.sh
fi

# SSH 접속 사용자를 Docker 그룹에 추가
TARGET_USER="${SUDO_USER:-$(id -un)}"
sudo usermod -aG docker "$TARGET_USER"

# Docker 시작 및 재부팅 시 자동 실행
sudo systemctl enable --now docker

# Spring 앱과 연결할 네트워크 생성
if ! sudo docker network inspect ai-network >/dev/null 2>&1; then
  sudo docker network create ai-network
fi

# 모델 저장용 볼륨 생성
sudo docker volume create ollama-data

# Ollama 실행 (CPU 사용)
if sudo docker container inspect ollama >/dev/null 2>&1; then
  sudo docker start ollama

  # 기존 컨테이너도 네트워크 연결 확인
  if [ -z "$(sudo docker inspect \
    --format '{{with index .NetworkSettings.Networks "ai-network"}}connected{{end}}' \
    ollama)" ]; then
    sudo docker network connect ai-network ollama
  fi

  sudo docker update --restart unless-stopped ollama
else
  sudo docker run -d \
    --name ollama \
    --restart unless-stopped \
    --network ai-network \
    -p 127.0.0.1:11434:11434 \
    -e OLLAMA_HOST=0.0.0.0:11434 \
    -v ollama-data:/root/.ollama \
    --log-opt max-size=10m \
    --log-opt max-file=3 \
    ollama/ollama:latest
fi

# Ollama 서버 준비 대기
ready=false

for ((i = 0; i < 60; i++)); do
  if sudo docker exec ollama ollama list >/dev/null 2>&1; then
    ready=true
    break
  fi
  sleep 2
done

if [ "$ready" != "true" ]; then
  echo "Ollama 시작 실패"
  sudo docker logs --tail 100 ollama
  exit 1
fi

# 모델 다운로드
sudo docker exec ollama ollama pull gemma3:1b

echo "Ollama 및 gemma3:1b 준비 완료"
sudo docker exec ollama ollama list

echo "SSH 재접속 후 sudo 없이 docker 명령을 사용할 수 있습니다."