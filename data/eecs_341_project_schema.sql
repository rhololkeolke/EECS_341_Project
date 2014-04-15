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
    id integer NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.brand OWNER TO rhol;

--
-- Name: brand_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE brand_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.brand_id_seq OWNER TO rhol;

--
-- Name: brand_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE brand_id_seq OWNED BY brand.id;


--
-- Name: customer; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE customer (
    loyalty_number integer NOT NULL,
    first_name character varying(100),
    middle_initial character varying(1),
    last_name character varying(100),
    birthdate date,
    gender character varying(1),
    join_date date,
    loyalty_points integer,
    CONSTRAINT customer_gender_check CHECK (((gender)::text = ANY (ARRAY[('M'::character varying)::text, ('F'::character varying)::text]))),
    CONSTRAINT customer_loyalty_points_check CHECK ((loyalty_points >= 0))
);


ALTER TABLE public.customer OWNER TO rhol;

--
-- Name: customer_email; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE customer_email (
    loyalty_number integer NOT NULL,
    email character varying NOT NULL,
    CONSTRAINT customer_email_email_check CHECK (((email)::text ~~ '_%@_%._%'::text))
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
    upc bigint NOT NULL,
    quantity integer NOT NULL,
    discount numeric NOT NULL,
    CONSTRAINT order_item_discount_check CHECK (((discount >= 0.0) AND (discount <= 1.0))),
    CONSTRAINT order_item_quantity_check CHECK ((quantity > 0))
);


ALTER TABLE public.order_item OWNER TO rhol;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE orders (
    id integer NOT NULL,
    order_date timestamp without time zone NOT NULL,
    store_id integer NOT NULL,
    loyalty_number integer NOT NULL,
    payment_type character varying(100) NOT NULL,
    shipping_loc integer,
    shipping_cost numeric,
    CONSTRAINT orders_check CHECK ((((shipping_loc IS NOT NULL) AND (shipping_cost IS NOT NULL)) OR ((shipping_loc IS NULL) AND ((shipping_cost IS NULL) OR (shipping_cost = (0)::numeric)))))
);


ALTER TABLE public.orders OWNER TO rhol;

--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.orders_id_seq OWNER TO rhol;

--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE orders_id_seq OWNED BY orders.id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product (
    upc bigint NOT NULL,
    name character varying(100) NOT NULL,
    "desc" text NOT NULL,
    size character varying(1),
    brand integer,
    unit_price numeric NOT NULL,
    CONSTRAINT product_size_check CHECK (((size)::text = ANY (ARRAY[('S'::character varying)::text, ('M'::character varying)::text, ('L'::character varying)::text]))),
    CONSTRAINT product_unit_price_check CHECK ((unit_price > (0)::numeric))
);


ALTER TABLE public.product OWNER TO rhol;

--
-- Name: product_location; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_location (
    shelf_id integer NOT NULL,
    upc bigint NOT NULL,
    amount integer NOT NULL,
    CONSTRAINT product_location_amount_check CHECK ((amount > 0))
);


ALTER TABLE public.product_location OWNER TO rhol;

--
-- Name: product_spec; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_spec (
    upc bigint NOT NULL,
    "desc" character varying(100) NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.product_spec OWNER TO rhol;

--
-- Name: product_type; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_type (
    upc bigint NOT NULL,
    id integer NOT NULL
);


ALTER TABLE public.product_type OWNER TO rhol;

--
-- Name: product_type_tree; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_type_tree (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    lft integer NOT NULL,
    rgt integer,
    CONSTRAINT product_type_tree_check CHECK ((lft < rgt)),
    CONSTRAINT product_type_tree_lft_check CHECK ((lft >= 0)),
    CONSTRAINT product_type_tree_rgt_check CHECK ((rgt > 0))
);


ALTER TABLE public.product_type_tree OWNER TO rhol;

--
-- Name: product_type_tree_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE product_type_tree_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_type_tree_id_seq OWNER TO rhol;

--
-- Name: product_type_tree_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE product_type_tree_id_seq OWNED BY product_type_tree.id;


--
-- Name: product_upc_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE product_upc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_upc_seq OWNER TO rhol;

--
-- Name: product_upc_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE product_upc_seq OWNED BY product.upc;


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
-- Name: shelf; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE shelf (
    id integer NOT NULL,
    store_id integer NOT NULL
);


ALTER TABLE public.shelf OWNER TO rhol;

--
-- Name: shelf_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE shelf_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shelf_id_seq OWNER TO rhol;

--
-- Name: shelf_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE shelf_id_seq OWNED BY shelf.id;


--
-- Name: shipping_location; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE shipping_location (
    id integer NOT NULL,
    loyalty_number integer NOT NULL,
    street1 character varying(100) NOT NULL,
    street2 character varying(100),
    city character varying(100) NOT NULL,
    state character varying(2) NOT NULL,
    zip integer NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.shipping_location OWNER TO rhol;

--
-- Name: shipping_location_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE shipping_location_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shipping_location_id_seq OWNER TO rhol;

--
-- Name: shipping_location_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE shipping_location_id_seq OWNED BY shipping_location.id;


--
-- Name: stock; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE stock (
    store_id integer NOT NULL,
    upc bigint NOT NULL,
    amount integer NOT NULL,
    CONSTRAINT stock_amount_check CHECK ((amount >= 0))
);


ALTER TABLE public.stock OWNER TO rhol;

--
-- Name: store; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    opening_date date NOT NULL,
    street1 character varying(100) NOT NULL,
    street2 character varying(100),
    city character varying(100) NOT NULL,
    state character varying(2) NOT NULL,
    zip integer NOT NULL
);


ALTER TABLE public.store OWNER TO rhol;

--
-- Name: store_closing; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store_closing (
    store_id integer NOT NULL,
    closed_date date NOT NULL,
    "desc" character varying(100)
);


ALTER TABLE public.store_closing OWNER TO rhol;

--
-- Name: store_hours; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store_hours (
    store_id integer NOT NULL,
    day_of_week character varying(2) NOT NULL,
    open_hour time without time zone NOT NULL,
    close_hour time without time zone NOT NULL,
    CONSTRAINT store_hours_day_of_week_check CHECK (((day_of_week)::text = ANY (ARRAY[('M'::character varying)::text, ('Tu'::character varying)::text, ('W'::character varying)::text, ('Th'::character varying)::text, ('F'::character varying)::text, ('Sa'::character varying)::text, ('Su'::character varying)::text])))
);


ALTER TABLE public.store_hours OWNER TO rhol;

--
-- Name: store_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.store_id_seq OWNER TO rhol;

--
-- Name: store_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE store_id_seq OWNED BY store.id;


--
-- Name: store_phone; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE store_phone (
    store_id integer NOT NULL,
    phone character varying NOT NULL,
    CONSTRAINT store_phone_phone_check CHECK (((phone)::text ~~ '(___)___-____'::text))
);


ALTER TABLE public.store_phone OWNER TO rhol;

--
-- Name: supplies; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE supplies (
    vendor_id integer NOT NULL,
    brand_id integer NOT NULL
);


ALTER TABLE public.supplies OWNER TO rhol;

--
-- Name: vendor; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE vendor (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    street1 character varying(100) NOT NULL,
    street2 character varying(100),
    city character varying(100) NOT NULL,
    state character varying(2) NOT NULL,
    zip integer NOT NULL
);


ALTER TABLE public.vendor OWNER TO rhol;

--
-- Name: vendor_id_seq; Type: SEQUENCE; Schema: public; Owner: rhol
--

CREATE SEQUENCE vendor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.vendor_id_seq OWNER TO rhol;

--
-- Name: vendor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rhol
--

ALTER SEQUENCE vendor_id_seq OWNED BY vendor.id;


--
-- Name: vendor_phone; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE vendor_phone (
    vendor_id integer NOT NULL,
    phone character varying(13) NOT NULL,
    CONSTRAINT vendor_phone_phone_check CHECK (((phone)::text ~~ '(___)___-____'::text))
);


ALTER TABLE public.vendor_phone OWNER TO rhol;

--
-- Name: vendor_purchase; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE vendor_purchase (
    store_id integer NOT NULL,
    vendor_id integer NOT NULL,
    upc bigint NOT NULL,
    purchase_date date NOT NULL,
    amount integer NOT NULL,
    unit_price numeric NOT NULL,
    CONSTRAINT vendor_purchase_amount_check CHECK ((amount > 0)),
    CONSTRAINT vendor_purchase_unit_price_check CHECK ((unit_price > (0)::numeric))
);


ALTER TABLE public.vendor_purchase OWNER TO rhol;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY brand ALTER COLUMN id SET DEFAULT nextval('brand_id_seq'::regclass);


--
-- Name: loyalty_number; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY customer ALTER COLUMN loyalty_number SET DEFAULT nextval('customer_loyalty_number_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders ALTER COLUMN id SET DEFAULT nextval('orders_id_seq'::regclass);


--
-- Name: upc; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product ALTER COLUMN upc SET DEFAULT nextval('product_upc_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type_tree ALTER COLUMN id SET DEFAULT nextval('product_type_tree_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY shelf ALTER COLUMN id SET DEFAULT nextval('shelf_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY shipping_location ALTER COLUMN id SET DEFAULT nextval('shipping_location_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store ALTER COLUMN id SET DEFAULT nextval('store_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor ALTER COLUMN id SET DEFAULT nextval('vendor_id_seq'::regclass);


--
-- Name: brand_name_key; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT brand_name_key UNIQUE (name);


--
-- Name: brand_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT brand_pkey PRIMARY KEY (id);


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
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: product_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_location
    ADD CONSTRAINT product_location_pkey PRIMARY KEY (shelf_id, upc);


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (upc);


--
-- Name: product_spec_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_spec
    ADD CONSTRAINT product_spec_pkey PRIMARY KEY (upc, "desc", value);


--
-- Name: product_type_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_type
    ADD CONSTRAINT product_type_pkey PRIMARY KEY (upc, id);


--
-- Name: product_type_tree_name_key; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_type_tree
    ADD CONSTRAINT product_type_tree_name_key UNIQUE (name);


--
-- Name: product_type_tree_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY product_type_tree
    ADD CONSTRAINT product_type_tree_pkey PRIMARY KEY (id);


--
-- Name: return_item_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY return_item
    ADD CONSTRAINT return_item_pkey PRIMARY KEY (order_id, upc, return_date);


--
-- Name: shelf_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY shelf
    ADD CONSTRAINT shelf_pkey PRIMARY KEY (id);


--
-- Name: shipping_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY shipping_location
    ADD CONSTRAINT shipping_location_pkey PRIMARY KEY (id, loyalty_number);


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
    ADD CONSTRAINT store_pkey PRIMARY KEY (id);


--
-- Name: supplies_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY supplies
    ADD CONSTRAINT supplies_pkey PRIMARY KEY (vendor_id, brand_id);


--
-- Name: vendor_phone_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY vendor_phone
    ADD CONSTRAINT vendor_phone_pkey PRIMARY KEY (vendor_id, phone);


--
-- Name: vendor_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY vendor
    ADD CONSTRAINT vendor_pkey PRIMARY KEY (id);


--
-- Name: vendor_purchase_pkey; Type: CONSTRAINT; Schema: public; Owner: rhol; Tablespace: 
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_pkey PRIMARY KEY (store_id, vendor_id, upc, purchase_date);


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
    ADD CONSTRAINT order_item_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id);


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
-- Name: orders_shipping_loc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_shipping_loc_fkey FOREIGN KEY (shipping_loc, loyalty_number) REFERENCES shipping_location(id, loyalty_number);


--
-- Name: orders_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY orders
    ADD CONSTRAINT orders_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: product_brand_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_brand_fkey FOREIGN KEY (brand) REFERENCES brand(id);


--
-- Name: product_location_shelf_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_location
    ADD CONSTRAINT product_location_shelf_id_fkey FOREIGN KEY (shelf_id) REFERENCES shelf(id);


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
-- Name: product_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type
    ADD CONSTRAINT product_type_id_fkey FOREIGN KEY (id) REFERENCES product_type_tree(id);


--
-- Name: product_type_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY product_type
    ADD CONSTRAINT product_type_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: return_item_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY return_item
    ADD CONSTRAINT return_item_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id);


--
-- Name: return_item_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY return_item
    ADD CONSTRAINT return_item_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: shelf_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY shelf
    ADD CONSTRAINT shelf_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: shipping_location_loyalty_number_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY shipping_location
    ADD CONSTRAINT shipping_location_loyalty_number_fkey FOREIGN KEY (loyalty_number) REFERENCES customer(loyalty_number);


--
-- Name: stock_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: stock_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: store_closing_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store_closing
    ADD CONSTRAINT store_closing_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: store_hours_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store_hours
    ADD CONSTRAINT store_hours_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: store_phone_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY store_phone
    ADD CONSTRAINT store_phone_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: supplies_brand_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY supplies
    ADD CONSTRAINT supplies_brand_id_fkey FOREIGN KEY (brand_id) REFERENCES brand(id);


--
-- Name: supplies_vendor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY supplies
    ADD CONSTRAINT supplies_vendor_id_fkey FOREIGN KEY (vendor_id) REFERENCES vendor(id);


--
-- Name: vendor_phone_vendor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_phone
    ADD CONSTRAINT vendor_phone_vendor_id_fkey FOREIGN KEY (vendor_id) REFERENCES vendor(id);


--
-- Name: vendor_purchase_store_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_store_id_fkey FOREIGN KEY (store_id) REFERENCES store(id);


--
-- Name: vendor_purchase_upc_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_upc_fkey FOREIGN KEY (upc) REFERENCES product(upc);


--
-- Name: vendor_purchase_vendor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rhol
--

ALTER TABLE ONLY vendor_purchase
    ADD CONSTRAINT vendor_purchase_vendor_id_fkey FOREIGN KEY (vendor_id) REFERENCES vendor(id);


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

