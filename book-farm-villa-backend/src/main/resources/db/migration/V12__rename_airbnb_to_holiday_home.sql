UPDATE properties
SET type = 'HOLIDAY_HOME'
WHERE type = 'AIRBNB';

UPDATE bookings
SET listing_type = 'HOLIDAY_HOME'
WHERE listing_type = 'AIRBNB';

UPDATE referrals
SET property_type = 'HOLIDAY_HOME'
WHERE UPPER(TRIM(property_type)) = 'AIRBNB';
