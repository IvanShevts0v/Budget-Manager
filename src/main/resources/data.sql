TRUNCATE TABLE expense_tags, expenses, wallets, tags, categories, users RESTART IDENTITY CASCADE;

INSERT INTO users (username) VALUES ('demo');

INSERT INTO categories (name) VALUES ('food'), ('transport'), ('entertainment');

INSERT INTO tags (name) VALUES ('essential'), ('recurring'), ('leisure');

INSERT INTO wallets (name, user_id)
SELECT 'Main', id FROM users WHERE username = 'demo';

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT v.descr, v.amt, v.d, w.id, c.id
FROM (VALUES
          ('Coffee', 150.00::numeric, DATE '2025-03-01', 'food'),
          ('Bus ticket', 50.00::numeric, DATE '2025-03-01', 'transport'),
          ('Lunch', 350.00::numeric, DATE '2025-03-02', 'food'),
          ('Netflix', 699.00::numeric, DATE '2025-03-02', 'entertainment')
     ) AS v(descr, amt, d, cat_name)
         JOIN wallets w ON w.name = 'Main'
         JOIN categories c ON c.name = v.cat_name;

INSERT INTO expense_tags (expense_id, tag_id)
SELECT e.id, t.id
FROM expenses e
         CROSS JOIN tags t
WHERE (e.description, t.name) IN (
    ('Coffee', 'essential'),
    ('Netflix', 'recurring'),
    ('Lunch', 'essential')
);
