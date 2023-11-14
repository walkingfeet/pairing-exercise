-- DN: using table to save notification as future development of project. This table may be used for service recovery for example or just history
CREATE TABLE orders_schema.orders_notification(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    notification_description text,
    organisation_id UUID not null CONSTRAINT FK_notification_of_orders_to_organisations REFERENCES organisations_schema.organisations,
    status text not null
);