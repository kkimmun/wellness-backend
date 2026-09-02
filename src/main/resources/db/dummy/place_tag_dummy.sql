-- Oracle dummy data for PLACE_TAG
--
-- * Every PLACE gets 1, 2, 3, or 4 tags in a repeating cycle.
-- * All TAG rows are used in round-robin order, so usage is distributed evenly.
-- * Existing PLACE_TAG pairs are preserved, and re-running this script does not
--   insert duplicate pairs.

MERGE INTO PLACE_TAG PT
USING (
    WITH RANKED_PLACES AS (
        SELECT
            P.PLACE_NO,
            ROW_NUMBER() OVER (ORDER BY P.PLACE_NO) - 1 AS PLACE_INDEX
        FROM PLACE P
    ),
    TAG_COUNT AS (
        SELECT COUNT(*) AS CNT
        FROM TAG
    ),
    RANKED_TAGS AS (
        SELECT
            T.TAG_NO,
            ROW_NUMBER() OVER (ORDER BY T.TAG_NO) - 1 AS TAG_INDEX
        FROM TAG T
    ),
    TAG_SLOTS AS (
        SELECT LEVEL AS SLOT_NO
        FROM DUAL
        CONNECT BY LEVEL <= 4
    )
    SELECT
        P.PLACE_NO,
        T.TAG_NO
    FROM RANKED_PLACES P
    CROSS JOIN TAG_SLOTS S
    CROSS JOIN TAG_COUNT TC
    JOIN RANKED_TAGS T
      ON T.TAG_INDEX = MOD(
             10 * FLOOR(P.PLACE_INDEX / 4)
             + (MOD(P.PLACE_INDEX, 4) * (MOD(P.PLACE_INDEX, 4) + 1) / 2)
             + S.SLOT_NO - 1,
             TC.CNT
         )
    WHERE TC.CNT > 0
      AND S.SLOT_NO <= MOD(P.PLACE_INDEX, 4) + 1
) SRC
ON (
       PT.PLACE_NO = SRC.PLACE_NO
   AND PT.TAG_NO = SRC.TAG_NO
)
WHEN NOT MATCHED THEN
    INSERT (PLACE_NO, TAG_NO)
    VALUES (SRC.PLACE_NO, SRC.TAG_NO);

COMMIT;

-- Verification: every place should have between 1 and 4 tags.
SELECT
    P.PLACE_NO,
    P.PLACE_NAME,
    COUNT(PT.TAG_NO) AS TAG_COUNT
FROM PLACE P
LEFT JOIN PLACE_TAG PT
  ON PT.PLACE_NO = P.PLACE_NO
GROUP BY P.PLACE_NO, P.PLACE_NAME
ORDER BY P.PLACE_NO;

-- Verification: tag usage should be distributed as evenly as possible.
SELECT
    T.TAG_NO,
    COUNT(PT.PLACE_NO) AS PLACE_COUNT
FROM TAG T
LEFT JOIN PLACE_TAG PT
  ON PT.TAG_NO = T.TAG_NO
GROUP BY T.TAG_NO
ORDER BY T.TAG_NO;
