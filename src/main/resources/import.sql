CREATE TABLE Product (
    id   INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

INSERT INTO Product(id, name,price) VALUES (1, 'Mackbock Pro',23000.00);
INSERT INTO Product(id, name,price) VALUES (2, 'Iphone 18 Pro Max 1TB',13000.00);
INSERT INTO Product(id, name,price) VALUES (3, 'Ipod',5000.00);
