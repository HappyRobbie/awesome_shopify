CREATE TRIGGER license_order_on_insert BEFORE INSERT ON orders FOR EACH ROW SET NEW.created_at = IFNULL(NEW.created_at, NOW()), NEW.updated_at = IFNULL(NEW.updated_at, NOW());
CREATE TRIGGER license_order_on_update BEFORE UPDATE ON orders FOR EACH ROW SET NEW.updated_at = IFNULL(NEW.updated_at, NOW());

CREATE INDEX shopify_order_idx ON orders(id, shop_id, shopify_order_number, token) USING HASH;