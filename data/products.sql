CREATE TABLE IF NOT EXISTS products (
    _ID INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    barcode INTEGER NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    best_before INTEGER NOT NULL,
    used INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS products_log (
    _ID INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    context INTEGER
);

CREATE TRIGGER IF NOT EXISTS on_add_product AFTER INSERT ON products
BEGIN
    INSERT INTO products_log (action, context) VALUES ('add', new._ID);
END;

CREATE TRIGGER IF NOT EXISTS on_use_product AFTER UPDATE ON products WHEN new.used = 1
BEGIN
    INSERT INTO products_log (action, context) VALUES ('mark_as_used', new._ID);
END;

CREATE TRIGGER IF NOT EXISTS on_remove_synced_product AFTER DELETE ON products WHEN old.product_id <> 0
BEGIN
    DELETE from products_log WHERE action = 'mark_as_used' AND context = old._ID;
    INSERT INTO products_log (action, context) VALUES ('remove', old._ID);
END;

CREATE TRIGGER IF NOT EXISTS on_remove_product AFTER DELETE ON products WHEN old.product_id = 0
BEGIN
    DELETE from products_log WHERE context = old._ID;
END;
