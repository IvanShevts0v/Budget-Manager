TRUNCATE TABLE expense_tags, expenses, wallets, tags, categories, users RESTART IDENTITY CASCADE;

INSERT INTO users (username) VALUES ('demo');

INSERT INTO categories (name) VALUES ('food'), ('transport'), ('entertainment');

INSERT INTO tags (name) VALUES ('essential'), ('recurring'), ('leisure');

INSERT INTO wallets (name, user_id)
SELECT 'Main', id FROM users WHERE username = 'demo';

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT 'Coffee', 150.00, DATE '2025-03-01', w.id, c.id
FROM wallets w, categories c
WHERE w.name = 'Main' AND c.name = 'food';

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT 'Bus ticket', 50.00, DATE '2025-03-01', w.id, c.id
FROM wallets w, categories c
WHERE w.name = 'Main' AND c.name = 'transport';

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT 'Lunch', 350.00, DATE '2025-03-02', w.id, c.id
FROM wallets w, categories c
WHERE w.name = 'Main' AND c.name = 'food';

INSERT INTO expenses (description, amount, date, wallet_id, category_id)
SELECT 'Netflix', 699.00, DATE '2025-03-02', w.id, c.id
FROM wallets w, categories c
WHERE w.name = 'Main' AND c.name = 'entertainment';

INSERT INTO expense_tags (expense_id, tag_id)
SELECT e.id, t.id FROM expenses e, tags t
WHERE e.description = 'Coffee' AND t.name = 'essential';

INSERT INTO expense_tags (expense_id, tag_id)
SELECT e.id, t.id FROM expenses e, tags t
WHERE e.description = 'Netflix' AND t.name = 'recurring';

INSERT INTO expense_tags (expense_id, tag_id)
SELECT e.id, t.id FROM expenses e, tags t
WHERE e.description = 'Lunch' AND t.name = 'essential';
