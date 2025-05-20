INSERT INTO users (id, email, password, username, role, created_at)
VALUES (
           gen_random_uuid(),
           'admin@gmail.com',
           '$2a$12$Gcs4cyG0OCr7QoPIQgsrz.OyxQjKilYkhs2EMtq1lku.8FEUlx69y',
           'AdminUser',
           'ADMIN',
           NOW()
       );

INSERT INTO brands (id, name, description, logo_url)
VALUES (
           gen_random_uuid(),
           'Example Brand',
           'A premium quality brand offering the best products on the market',
           'https://example.com/logo.png'
       );

INSERT INTO products (id, name, description, brand_id, price, stock_quantity, created_at)
VALUES (
           gen_random_uuid(),
           'Premium Product',
           'High-quality product with excellent features',
           (SELECT id FROM brands WHERE name = 'Example Brand' LIMIT 1),
           99.99,
           100,
           NOW()
       );