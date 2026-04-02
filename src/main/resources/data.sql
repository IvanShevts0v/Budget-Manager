TRUNCATE TABLE expense_tags, expenses, wallets, tags, categories, users RESTART IDENTITY CASCADE;

INSERT INTO users (username) VALUES ('demo');

INSERT INTO categories (name)
SELECT unnest(ARRAY['food', 'transport', 'entertainment']::text[]);

INSERT INTO tags (name)
SELECT unnest(ARRAY['essential', 'recurring', 'leisure']::text[]);

INSERT INTO wallets (name, user_id)
SELECT 'Main', id FROM users WHERE username = 'demo';

CREATE TEMP TABLE expense_seed (
    descr text,
    amt numeric,
    d date,
    cat_key int,
    tag_name text
);

INSERT INTO expense_seed (descr, amt, d, cat_key, tag_name)
VALUES ('Coffee', 150.00::numeric, DATE '2025-03-01', 1, 'essential'),
       ('Bus ticket', 50.00::numeric, DATE '2025-03-01', 2, null),
       ('Lunch', 350.00::numeric, DATE '2025-03-02', 1, 'essential'),
       ('Netflix', 699.00::numeric, DATE '2025-03-02', 3, 'recurring');

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT s.descr,
       s.amt,
       s.d,
       w.id,
       c.id
FROM expense_seed s
         JOIN wallets w ON w.name = 'Main'
         JOIN categories c ON c.name =
             CASE s.cat_key
                 WHEN 1 THEN 'food'
                 WHEN 2 THEN 'transport'
                 WHEN 3 THEN 'entertainment'
             END;

INSERT INTO expense_tags (expense_id, tag_id)
SELECT e.id,
       t.id
FROM expense_seed s
         JOIN expenses e ON e.description = s.descr
         JOIN tags t ON t.name = s.tag_name
WHERE s.tag_name IS NOT NULL;
