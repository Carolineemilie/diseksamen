use cbsexam; 
SELECT
orders.id as order_id,
orders.order_total,
orders.created_at as order_created_at,
orders.updated_at as order_updated_at,
user.id as user_id,
user.email as user_email,
user.password as user_password,
user.last_name as user_lastname,
user.first_name as user_firstname,
line_item.id as line_item_id,
line_item.quantity as line_item_quantity,
line_item.price as line_item_price,
product.id as product_id,
product.stock as product_stock,
product.description as product_description,
product.price as product_price,
product.sku as product_sku,
product.product_name,
billing.id as billing_id,
billing.name as billing_name,
billing.street_address as billing_street_adress,
billing.city as billing_city,
billing.zipcode as billing_zipcode,
shipping.id as shipping_id,
shipping.name as shipping_name,
shipping.street_address as shipping_street_adress,
shipping.city as shipping_city,
shipping.zipcode as shipping_zipcode 
FROM orders 
LEFT JOIN user ON orders.user_id = user.id
LEFT JOIN line_item ON orders.id = line_item.order_id
LEFT JOIN product ON line_item.product_id = product.id
LEFT JOIN address as billing ON orders.billing_address_id = billing.id
LEFT JOIN address as shipping ON orders.shipping_address_id = shipping.id;
 