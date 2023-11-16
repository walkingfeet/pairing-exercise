-- DN: History of shipments for future audit or any argue with client
CREATE TABLE orders_schema.order_product_shipment_history(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    product_id UUID CONSTRAINT order_product_shipment_history_to_products REFERENCES orders_schema.products(id),
    amount int not null CONSTRAINT amount_positive CHECK (amount > 0),
    created timestamp not null DEFAULT current_timestamp
    -- no updated as this table should be immutable
);