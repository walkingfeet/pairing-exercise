CREATE TABLE orders_schema.products(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name text not null,
    organisation_id UUID not null CONSTRAINT FK_product_to_organisation_id REFERENCES organisations_schema.organisations(id),
    created timestamp not null DEFAULT current_timestamp,
    updated timestamp not null DEFAULT current_timestamp
);