TRUNCATE TABLE expense_tags, expenses, wallets, tags, categories, users RESTART IDENTITY CASCADE;

INSERT INTO users (username) VALUES ('demo');

INSERT INTO categories (name)
SELECT unnest(ARRAY['food', 'transport', 'entertainment']::text[]);

INSERT INTO tags (name)
SELECT unnest(ARRAY['essential', 'recurring', 'leisure']::text[]);

INSERT INTO wallets (name, user_id)
SELECT 'Main', id FROM users WHERE username = 'demo';

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT v.descr,
       v.amt,
       v.d,
       w.id,
       c.id
FROM (VALUES
          ('Coffee', 150.00::numeric, DATE '2025-03-01', 1),
          ('Bus ticket', 50.00::numeric, DATE '2025-03-01', 2),
          ('Lunch', 350.00::numeric, DATE '2025-03-02', 1),
          ('Netflix', 699.00::numeric, DATE '2025-03-02', 3)
     ) AS v(descr, amt, d, cat_key)
         JOIN wallets w ON w.name = 'Main'
         JOIN categories c ON c.name =
             CASE v.cat_key
                 WHEN 1 THEN 'food'
                 WHEN 2 THEN 'transport'
                 WHEN 3 THEN 'entertainment'
             END;

INSERT INTO expense_tags (expense_id, tag_id)
SELECT e.id,
       t.id
FROM expenses e
         JOIN tags t ON t.name = CASE
             WHEN e.description IN ('Coffee', 'Lunch') THEN 'essential'
             WHEN e.description = 'Netflix' THEN 'recurring'
         END
WHERE e.description IN ('Coffee', 'Lunch', 'Netflix');
