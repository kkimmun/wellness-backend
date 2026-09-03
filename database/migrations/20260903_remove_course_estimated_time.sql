-- Oracle SQL. Run as the application schema owner before starting the updated backend.
-- This script is not run automatically by Spring Boot.
-- Back up the original values before removing the obsolete column. The backup stays available.
-- If the backup name already exists while the source column still exists, stop for review.
-- Re-running after the column has been removed is a no-op.
DECLARE
    column_count NUMBER;
    backup_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO column_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'COURSE' AND COLUMN_NAME = 'ESTIMATED_TIME';

    IF column_count > 0 THEN
        SELECT COUNT(*) INTO backup_count
        FROM USER_TABLES
        WHERE TABLE_NAME = 'COURSE_TIME_BACKUP_20260903';
        IF backup_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'COURSE_TIME_BACKUP_20260903 already exists. Verify the backup before continuing.');
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE COURSE_TIME_BACKUP_20260903 AS SELECT COURSE_NO, ESTIMATED_TIME FROM COURSE';
        EXECUTE IMMEDIATE 'ALTER TABLE COURSE DROP COLUMN ESTIMATED_TIME';
    END IF;
END;
/

-- Post-check: zero rows means the column has been removed.
SELECT COLUMN_NAME
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'COURSE' AND COLUMN_NAME = 'ESTIMATED_TIME';
