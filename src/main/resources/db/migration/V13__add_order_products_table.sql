CREATE TABLE orders_schema.product_orders(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    order_id UUID not null CONSTRAINT FK_product_orders_to_organisation_id REFERENCES orders_schema.orders(id),
    product_id UUID not null CONSTRAINT FK_product_orders_to_product_id REFERENCES orders_schema.products(id),
    products_amount_in_order int not null CONSTRAINT products_amount_in_order CHECK (products_amount_in_order > 0),
    -- DN: There is a good tone to have such constraint, but it would be double checked in application too, it's ok until performance is not suffer
    -- DN: Also application should be responsible for such checks - we would not cover database error - if it catches - means that application has a bug and should be alerted
    products_amount_shipped_by_merchant int not null CONSTRAINT products_amount_shipped_by_merchant_is_less_than_product_amount_in_order
            CHECK (products_amount_shipped_by_merchant > 0 and products_amount_shipped_by_merchant <= products_amount_in_order),
    created timestamp not null DEFAULT current_timestamp,
    updated timestamp not null DEFAULT current_timestamp,
    -- In one order should not have more than one order
    constraint UNIQ_product_id_and_order_id UNIQUE (order_id, product_id)
);

-- Create an index for order_id and product_id as it using in search by order and product
CREATE INDEX idx_order_id_product_id ON orders_schema.product_orders (order_id, product_id);