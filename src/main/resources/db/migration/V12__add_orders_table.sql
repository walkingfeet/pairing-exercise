CREATE TABLE orders_schema.orders(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    bayer_id UUID not null CONSTRAINT FK_orders_buyer_id_to_organisation_id REFERENCES organisations_schema.organisations(id),
    merchant_id UUID not null CONSTRAINT FK_orders_merchant_id_to_organisation_id REFERENCES organisations_schema.organisations(id),
    total_price decimal not null,
    status text not null,
    created timestamp not null DEFAULT current_timestamp,
    updated timestamp not null DEFAULT current_timestamp
);