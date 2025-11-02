INSERT INTO person (id, username, password, role)
SELECT 0, 'admin', '{bcrypt}$2a$10$u0UMhsha8s8Cdr/d8.hW3uKZGmBAU3AnD.dAlQ72AOkDFTOsB34QG', 3
WHERE NOT EXISTS (SELECT 1 FROM person LIMIT 1);