--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: brand; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE brand (
    name character varying(30) NOT NULL
);


ALTER TABLE public.brand OWNER TO rhol;

--
-- Name: customer; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE customer (
    loyalty_number integer NOT NULL,
    first_name character varying(20),
    middle_initial character(1),
    last_name character varying(20),
    birthdate date,
    gender character(1),
    join_date date,
    loyalty_points integer,
    street1 character varying(20),
    street2 character varying(20),
    zip integer,
    city character varying(20),
    state character(2),
    CONSTRAINT customer_gender_check CHECK ((gender = ANY (ARRAY['M'::bpchar, 'F'::bpchar]))),
    CONSTRAINT customer_loyalty_points_check CHECK ((loyalty_points >= 0))
);


ALTER TABLE public.customer OWNER TO rhol;

--
-- Name: customer_email; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE customer_email (
    loyalty_number integer NOT NULL,
    email character varying(50) NOT NULL
);


ALTER TABLE public.customer_email OWNER TO rhol;

--
-- Name: customer_loyalty_number_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE customer_loyalty_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.customer_loyalty_number_seq OWNER TO rhol;

--
-- Name: customer_loyalty_number_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE customer_loyalty_number_seq OWNED BY customer.loyalty_number;


--
-- Name: customer_phone; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE customer_phone (
    loyalty_number integer NOT NULL,
    phone character varying(13) NOT NULL,
    CONSTRAINT customer_phone_phone_check CHECK (((phone)::text ~~ '(___)___-____'::text))
);


ALTER TABLE public.customer_phone OWNER TO rhol;

--
-- Name: order_item; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE order_item (
    order_id integer NOT NULL,
    upc integer NOT NULL,
    quantity integer NOT NULL,
    discount numeric,
    CONSTRAINT order_item_quantity_check CHECK ((quantity > 0))
);


ALTER TABLE public.order_item OWNER TO rhol;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE orders (
    order_id integer NOT NULL,
    order_date date NOT NULL,
    store_id integer NOT NULL,
    loyalty_number integer NOT NULL,
    payment_type character varying(20) NOT NULL,
    shipping_loc_id integer,
    shipping_cost numeric,
    CONSTRAINT orders_check CHECK ((((shipping_loc_id IS NOT NULL) AND (shipping_cost IS NOT NULL)) OR ((shipping_loc_id IS NULL) AND (shipping_cost IS NULL))))
);


ALTER TABLE public.orders OWNER TO rhol;

--
-- Name: orders_order_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE orders_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.orders_order_id_seq OWNER TO rhol;

--
-- Name: orders_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE orders_order_id_seq OWNED BY orders.order_id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product (
    upc bigint NOT NULL,
    name character varying(100) NOT NULL,
    description text NOT NULL,
    size character(1),
    brand character varying(30),
    CONSTRAINT product_size_check CHECK ((size = ANY (ARRAY['S'::bpchar, 'M'::bpchar, 'L'::bpchar])))
);


ALTER TABLE public.product OWNER TO rhol;

--
-- Name: product_location; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_location (
    store_id integer NOT NULL,
    upc integer NOT NULL,
    shelf_id integer NOT NULL,
    amount integer,
    CONSTRAINT product_location_amount_check CHECK ((amount >= 0))
);


ALTER TABLE public.product_location OWNER TO rhol;

--
-- Name: product_spec; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_spec (
    upc bigint NOT NULL,
    type character varying(50) NOT NULL,
    amount numeric NOT NULL,
    unit character varying(20) NOT NULL
);


ALTER TABLE public.product_spec OWNER TO rhol;

--
-- Name: product_type; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_type (
    upc bigint NOT NULL,
    type_id integer NOT NULL
);


ALTER TABLE public.product_type OWNER TO rhol;

--
-- Name: product_type_class; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_type_class (
    type_id integer NOT NULL,
    name character varying(30) NOT NULL,
    parent_category integer,
    lft integer NOT NULL,
    rgt integer NOT NULL
);


ALTER TABLE public.product_type_class OWNER TO rhol;

--
-- Name: product_type_class_type_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE product_type_class_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_type_class_type_id_seq OWNER TO rhol;

--
-- Name: product_type_class_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE product_type_class_type_id_seq OWNED BY product_type_class.type_id;


--
-- Name: return_item; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE return_item (
    order_id integer NOT NULL,
    upc integer NOT NULL,
    return_date date NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT return_item_quantity_check CHECK ((quantity > 0))
);


ALTER TABLE public.return_item OWNER TO rhol;

--
-- Name: shipping_location; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE shipping_location (
    shipping_loc_id integer NOT NULL,
    loyalty_number integer NOT NULL,
    street1 character varying(20) NOT NULL,
    street2 character varying(20),
    city character varying(20) NOT NULL,
    state character(2) NOT NULL,
    zip integer NOT NULL
);


ALTER TABLE public.shipping_location OWNER TO rhol;

--
-- Name: shipping_location_shipping_loc_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE shipping_location_shipping_loc_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shipping_location_shipping_loc_id_seq OWNER TO rhol;

--
-- Name: shipping_location_shipping_loc_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE shipping_location_shipping_loc_id_seq OWNED BY shipping_location.shipping_loc_id;


--
-- Name: stock; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE stock (
    store_id integer NOT NULL,
    upc integer NOT NULL,
    amount integer NOT NULL,
    unit_price numeric NOT NULL,
    CONSTRAINT stock_amount_check CHECK ((amount >= 0)),
    CONSTRAINT stock_unit_price_check CHECK ((unit_price >= (0)::numeric))
);


ALTER TABLE public.stock OWNER TO rhol;

--
-- Name: store; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store (
    store_id integer NOT NULL,
    opening_date date NOT NULL,
    street1 character varying(50) NOT NULL,
    street2 character varying(50),
    city character varying(20) NOT NULL,
    state character(2) NOT NULL,
    zip integer NOT NULL,
    name character varying(75)
);


ALTER TABLE public.store OWNER TO rhol;

--
-- Name: store_closing; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store_closing (
    store_id integer NOT NULL,
    closed_date date NOT NULL,
    description text
);


ALTER TABLE public.store_closing OWNER TO rhol;

--
-- Name: store_hours; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store_hours (
    store_id integer NOT NULL,
    day_of_week character(2) NOT NULL,
    open_hour time without time zone,
    close_hour time without time zone,
    CONSTRAINT store_hours_day_of_week_check CHECK ((day_of_week = ANY (ARRAY['M'::bpchar, 'Tu'::bpchar, 'W'::bpchar, 'Th'::bpchar, 'F'::bpchar, 'Sa'::bpchar, 'Su'::bpchar])))
);


ALTER TABLE public.store_hours OWNER TO rhol;

--
-- Name: store_phone; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store_phone (
    store_id integer NOT NULL,
    phone character varying(13) NOT NULL,
    CONSTRAINT store_phone_phone_check CHECK (((phone)::text ~~ '(___)___-____'::text))
);


ALTER TABLE public.store_phone OWNER TO rhol;

--
-- Name: store_store_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE store_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.store_store_id_seq OWNER TO rhol;

--
-- Name: store_store_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE store_store_id_seq OWNED BY store.store_id;


--
-- Name: supplies; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE supplies (
    vendor_name character varying(20) NOT NULL,
    brand_name character varying(20) NOT NULL
);


ALTER TABLE public.supplies OWNER TO rhol;

--
-- Name: vendor; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE vendor (
    name character varying(20) NOT NULL,
    street1 character varying(20) NOT NULL,
    street2 character varying(20),
    city character varying(20) NOT NULL,
    state character(2) NOT NULL,
    zip integer NOT NULL
);


ALTER TABLE public.vendor OWNER TO rhol;

--
-- Name: vendor_phone; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE vendor_phone (
    vendor_name character varying(20) NOT NULL,
    phone character varying(13) NOT NULL,
    CONSTRAINT vendor_phone_phone_check CHECK (((phone)::text ~~ '(___)___-____'::text))
);


ALTER TABLE public.vendor_phone OWNER TO rhol;

--
-- Name: vendor_purchase; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE vendor_purchase (
    store_id integer NOT NULL,
    vendor_name character varying(20) NOT NULL,
    upc integer NOT NULL,
    purchase_date date NOT NULL,
    amount integer NOT NULL,
    unit_price numeric NOT NULL,
    CONSTRAINT vendor_purchase_amount_check CHECK ((amount > 0)),
    CONSTRAINT vendor_purchase_unit_price_check CHECK ((unit_price > (0)::numeric))
);


ALTER TABLE public.vendor_purchase OWNER TO rhol;

--
-- Name: loyalty_number; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY customer ALTER COLUMN loyalty_number SET DEFAULT nextval('customer_loyalty_number_seq'::regclass);


--
-- Name: order_id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders ALTER COLUMN order_id SET DEFAULT nextval('orders_order_id_seq'::regclass);


--
-- Name: type_id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type_class ALTER COLUMN type_id SET DEFAULT nextval('product_type_class_type_id_seq'::regclass);


--
-- Name: shipping_loc_id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY shipping_location ALTER COLUMN shipping_loc_id SET DEFAULT nextval('shipping_location_shipping_loc_id_seq'::regclass);


--
-- Name: store_id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store ALTER COLUMN store_id SET DEFAULT nextval('store_store_id_seq'::regclass);


--
-- Name: brand_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT brand_pkey PRIMARY KEY (name);


--
-- Name: customer_email_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY customer_email
    ADD CONSTRAINT customer_email_pkey PRIMARY KEY (loyalty_number, email);


--
-- Name: customer_phone_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY customer_phone
    ADD CONSTRAINT customer_phone_pkey PRIMARY KEY (loyalty_number, phone);


--
-- Name: customer_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (loyalty_number);


--
-- Name: order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (order_id, upc);


--
-- Name: orders_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);


--
-- Name: product_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_location
    ADD CONSTRAINT product_location_pkey PRIMARY KEY (store_id, upc, shelf_id);


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (upc);


--
-- Name: product_spec_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_spec
    ADD CONSTRAINT product_spec_pkey PRIMARY KEY (upc, type, amount, unit);


--
-- Name: product_type_class_name_key; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_type_class
    ADD CONSTRAINT product_type_class_name_key UNIQUE (name);


--
-- Name: product_type_class_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_type_class
    ADD CONSTRAINT product_type_class_pkey PRIMARY KEY (type_id);


--
-- Name: product_type_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_type
    ADD CONSTRAINT product_type_pkey PRIMARY KEY (upc, type_id);


--
-- Name: return_item_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY return_item
    ADD CONSTRAINT return_item_pkey PRIMARY KEY (order_id, upc, return_date);


--
-- Name: shipping_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY shipping_location
    ADD CONSTRAINT shipping_location_pkey PRIMARY KEY (shipping_loc_id, loyalty_number);


--
-- Name: stock_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_pkey PRIMARY KEY (store_id, upc);


--
-- Name: store_closing_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY store_closing
    ADD CONSTRAINT store_closing_pkey PRIMARY KEY (store_id, closed_date);


--
-- Name: store_hours_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY store_hours
    ADD CONSTRAINT store_hours_pkey PRIMARY KEY (store_id, day_of_week);


--
-- Name: store_phone_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY store_phone
    ADD CONSTRAINT store_phone_pkey PRIMARY KEY (store_id, phone);


--
-- Name: store_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY store
    ADD CONSTRAINT store_pkey PRIMARY KEY (store_id);


--
-- Name: supplies_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY supplies
    ADD CONSTRAINT supplies_pkey PRIMARY KEY (vendor_name, brand_name);


--
-- Name: vendor_phone_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY vendor_phone
    ADD CONSTRAINT vendor_phone_pkey PRIMARY KEY (vendor_name, phone);


--
-- Name: vendor_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY vendor
    ADD CONSTRAINT vendor_pkey PRIMARY KEY (name);


--
-- Name: vendor_purchase_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_pkey PRIMARY KEY (store_id, vendor_name, upc, purchase_date);


--
-- Name: customer_email_loyalty_number_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY customer_email
    ADD CONSTRAINT customer_email_loyalty_number_fkey FOREIGN KEY (loyalty_number) REFERENCES customer(loyalty_number);


--
-- Name: customer_phone_loyalty_number_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY customer_phone
    ADD CONSTRAINT customer_phone_loyalty_number_fkey FOREIGN KEY (loyalty_number) REFERENCES customer(loyalty_number);


--
-- Name: order_item_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY order_item
    ADD CONSTRAINT order_item_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(order_id);


--
-- Name: order_item_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY order_item
    ADD CONSTRAINT order_item_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: orders_loyalty_number_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_loyalty_number_fkey FOREIGN KEY (loyalty_number) REFERENCES customer(loyalty_number);


--
-- Name: orders_shipping_loc_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_shipping_loc_id_fkey FOREIGN KEY (shipping_loc_id, loyalty_number) REFERENCES shipping_location(shipping_loc_id, loyalty_number);


--
-- Name: orders_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: product_brand_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_brand_fkey FOREIGN KEY (brand) REFERENCES brand(name);


--
-- Name: product_location_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_location
    ADD CONSTRAINT product_location_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: product_location_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_location
    ADD CONSTRAINT product_location_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: product_spec_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_spec
    ADD CONSTRAINT product_spec_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: product_type_class_parent_category_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type_class
    ADD CONSTRAINT product_type_class_parent_category_fkey FOREIGN KEY (parent_category) REFERENCES product_type_class(type_id);


--
-- Name: product_type_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type
    ADD CONSTRAINT product_type_type_id_fkey FOREIGN KEY (type_id) REFERENCES product_type_class(type_id);


--
-- Name: product_type_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type
    ADD CONSTRAINT product_type_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: return_item_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY return_item
    ADD CONSTRAINT return_item_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(order_id);


--
-- Name: return_item_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY return_item
    ADD CONSTRAINT return_item_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: shipping_location_loyalty_number_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY shipping_location
    ADD CONSTRAINT shipping_location_loyalty_number_fkey FOREIGN KEY (loyalty_number) REFERENCES customer(loyalty_number);


--
-- Name: stock_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: stock_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: store_closing_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store_closing
    ADD CONSTRAINT store_closing_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: store_hours_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store_hours
    ADD CONSTRAINT store_hours_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: store_phone_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store_phone
    ADD CONSTRAINT store_phone_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: supplies_brand_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY supplies
    ADD CONSTRAINT supplies_brand_name_fkey FOREIGN KEY (brand_name) REFERENCES brand(name);


--
-- Name: supplies_vendor_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY supplies
    ADD CONSTRAINT supplies_vendor_name_fkey FOREIGN KEY (vendor_name) REFERENCES vendor(name);


--
-- Name: vendor_phone_vendor_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_phone
    ADD CONSTRAINT vendor_phone_vendor_name_fkey FOREIGN KEY (vendor_name) REFERENCES vendor(name);


--
-- Name: vendor_purchase_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(store_id);


--
-- Name: vendor_purchase_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: vendor_purchase_vendor_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_vendor_name_fkey FOREIGN KEY (vendor_name) REFERENCES vendor(name);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

