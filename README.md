Pair Programming Exercise for Billie
=============
### The Requirements

The way a business like Billie works is this:

```
A business buyer goes to a merchant's e-commerce platform catering and buys goods. 
At the checkout, the buyer chooses Billie as a payment method and checks out via our 
widget on the merchant's site. Upon shipment of the goods, the merchant sends us
the information about the shipped items and is paid immediately.
Depending on the availability of the items the merchant can ship all the items
at once or in separate shipments.
Example: the buyer bought 3 items and the merchant shipped the 1st item the next day
and the 2 other items one week later.
Billie also issues an invoice to the buyer. The buyer is then invoiced by Billie
and they pay the invoice
```

At this point, we have built an API to map simple organizations, but not much else.  
There are a lot of features still to build!!!

### The Exercise

The task is to implement one of the most important business requirements:

> The ability for the merchant to notify Billie of shipment of an order, so they can get paid.
> The merchant is not required to send a list of the shipped items but the sum of the shipments
> should not exceed the total order amount.

In order to implement this, please make a fork of this repo and when done with the implementation, send us a link to
your repo. The time spent on this assignment is usually between 2 and 4 hours. 
If you feel that that wasn't enough please describe in a document what would you add if you had more time.

Strong hint: we are fans of TDD, DDD, and clean code. 
Feel free to change the folder structure or anything else in the project

### The Tech Stuff
#### Prerequisites
We assume that you have docker, docker compose, and Java 15 installed, and can run gradle

Tests using testcontainers, make sure to have docker daemon launched

Enjoy local start with 

```bash
./gradlew build && docker compose up --build
```
Docs are
```
http://localhost:8080/swagger-ui/index.html
```

### Solution
Implementation steps

1. Database structure of code - 
   1. Remove cities as there is no usage in code
   2. Add orders schema to separate domains
   3. Add table products to store product which is sold by merchant.
   This table may be expanded in future for storing some additional info, 
   for example reference to special product on merchant website. 
   In the exercise there is need to save delivered items
   4. Add orders table - it will be created during process of checkout
   5. Add order products table - creates during checkout process of the order and will be updated during shipment process
   6. Add order history table for audit
   7. Add notification table as a recovery. 
   I think notifications are out of scope of exercise. 
   To show additional logic and have opportunity to send all notifications to queue after application crash 
   creating this table
2. Correct testing processes - added test containers for creating test without start docker compose up
3. Add repository for tables and tests for it
4. Add service logic and tests for it
5. Add controllers and tests for it
6. Finish docker compose up only for manual setup

What will be not covered - 
1. Authentication - It's separate task - 
there are at least three different roles - 
   1. Admin role that may create and approve organisations, add countries etc.
   2. Merchant role which may add products
   3. Buyer role which can create orders

I'm sure that there are different APIs, but for exercise - left as it is

2. Updating dependencies - decided to not change as it currently works and not important for structured code. 
Also updating it depends on rule of the team - for example we currently use only java 11, or kotlin 1.5 etc.
3. Notifications - after order merchant should be notified, after completed shipments merchant and buyer should be notified. 
I would use some queue service to implement this logic. It's out of scope for current exercise
4. Renaming Add_countries.sql, Add_cities.sql, add_schema.sql - are not possible due to flyway checks 
5. Cleanup of organisation and countries - would add foreign keys and removed ID from countries as country code is primary key by itself.
Out of scope - as there may be additional cases to have one
6. Using real client instead of mockmvc - real http client allows test application clearer than mockmvc - and have better cover
7. Contract first - public API may have contract first principle. Out of scope
8. Traefik setup - removed it for simplicity of the project - it requires additional setup which not suites in time
9. Setup and add Ktlint

DN comments - They are not part of documentation but comments to understand why solution was made

Used style of projects as in organisation - repository model and common view model