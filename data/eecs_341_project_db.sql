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
    upc integer NOT NULL,
    name character varying(30) NOT NULL,
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
    upc integer NOT NULL,
    type character varying(20) NOT NULL,
    amount numeric NOT NULL,
    unit character varying(20) NOT NULL
);


ALTER TABLE public.product_spec OWNER TO rhol;

--
-- Name: product_type; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_type (
    upc integer NOT NULL,
    type_id integer NOT NULL
);


ALTER TABLE public.product_type OWNER TO rhol;

--
-- Name: product_type_class; Type: TABLE; Schema: public; Owner: rhol; Tablespace: 
--

CREATE TABLE product_type_class (
    type_id integer NOT NULL,
    name character varying(30) NOT NULL,
    parent_category integer
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
-- Data for Name: brand; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY brand (name) FROM stdin;
Acer
Antec
Apple
ASUS
Brother
Corsair
Dell
EVGA
Hewlett-Packard
Intel
Lenovo
LG
Microsoft
PowerSpec
Tenda
Toshiba
Western Digital
\.


--
-- Data for Name: customer; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY customer (loyalty_number, first_name, middle_initial, last_name, birthdate, gender, join_date, loyalty_points, street1, street2, zip, city, state) FROM stdin;
\.


--
-- Data for Name: customer_email; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY customer_email (loyalty_number, email) FROM stdin;
\.


--
-- Name: customer_loyalty_number_seq; Type: SEQUENCE SET; Schema: public; Owner: rhol
--

SELECT pg_catalog.setval('customer_loyalty_number_seq', 1, false);


--
-- Data for Name: customer_phone; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY customer_phone (loyalty_number, phone) FROM stdin;
\.


--
-- Data for Name: order_item; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY order_item (order_id, upc, quantity, discount) FROM stdin;
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY orders (order_id, order_date, store_id, loyalty_number, payment_type, shipping_loc_id, shipping_cost) FROM stdin;
\.


--
-- Name: orders_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: rhol
--

SELECT pg_catalog.setval('orders_order_id_seq', 1, false);


--
-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY product (upc, name, description, size, brand) FROM stdin;
\.


--
-- Data for Name: product_location; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY product_location (store_id, upc, shelf_id, amount) FROM stdin;
\.


--
-- Data for Name: product_spec; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY product_spec (upc, type, amount, unit) FROM stdin;
\.


--
-- Data for Name: product_type; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY product_type (upc, type_id) FROM stdin;
\.


--
-- Data for Name: product_type_class; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY product_type_class (type_id, name, parent_category) FROM stdin;
1	Computers	\N
2	Computer Parts	\N
3	Electronics	\N
4	Networking	\N
5	Accessories	\N
6	Gaming	\N
7	Software	\N
8	Books	\N
9	Desktops	1
10	Ultrabooks	1
11	Laptops/Netbooks	1
12	Apple Desktops	1
13	Apple Laptops	1
14	Monitors	1
15	Apple iPads	1
16	Tablets	1
17	Printers	1
18	Keyboards	1
19	Mice	1
20	Scanners	1
21	Projectors	1
22	All-in-One Desktops	9
23	Laptop Accessories	11
24	Mac Hardware	12
25	Mac Accessories	12
26	Mac Laptop Accessories	13
27	Processors/CPUs	2
28	Motherboards	2
29	Hard Drives	2
30	Computer Memory/RAM	2
31	Video Cards	2
32	TV Tuners	2
33	Power Supplies	2
34	Computer Cases	2
35	CD/DVD/Blu-ray Burners	2
36	Air & Water Cooling	2
37	Sound Cards	2
38	Controller Cards	2
39	Case Accessories	2
40	Tools	2
41	AMD CPU/Motherboard Bundles	27
42	Intel CPU/Motherboard Bundles	27
43	LCD, Plasma HDTVs	3
44	iPods & MP3 Players	3
45	GPS	3
46	Digital Photography	3
47	Flash Memory	3
48	USB Flash Drives	3
49	Webcams & Video Editing	3
50	Digital Photo Frames	3
51	Hobby Electronics	3
52	Phones & Accessories	3
53	Security & Home Automation	3
54	Wall Mounts & Stands	43
55	MP3 Accessories	44
56	Digital Cameras	46
57	Digital SLR Cameras	46
58	Camcorders	46
59	Photo Accessories	46
60	Arduino Compatible	51
61	Raspberry Pi	51
62	Wireless Networking	4
63	Wired Networking	4
64	Network Attached Storage (NAS)	4
65	Networking Cables	4
66	Modems	4
67	Networking Tools	4
68	Wireless Routers	62
69	Wireless Cards	62
70	Wireless Access Points	62
71	Antennas, Signal Boosters	62
72	Wired Routers	63
73	Wired Cards	63
74	Wired Hubs, Switches	63
75	Ink & Toner Finder	5
76	Carrying Cases	5
77	Paper	5
78	Labels	5
79	CD/DVD/Blu-ray Media	5
80	Cables & Adapters	5
81	Batteries, Chargers	5
82	UPS, Surge Protection	5
83	KVM	5
84	Wristrest, Mousepads	5
85	Office Supplies	5
86	Cleaning, Maintenance	5
87	Speakers, Microphones	5
88	Consoles	6
89	Handheld	6
90	Windows	6
91	XBOX ONE	6
92	XBOX 360	6
93	PS4	6
94	PS3	6
95	Wii/Wii U	6
96	Macintosh	6
97	PS2	6
98	Game Guides	6
99	Windows Server	7
\.


--
-- Name: product_type_class_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: rhol
--

SELECT pg_catalog.setval('product_type_class_type_id_seq', 99, true);


--
-- Data for Name: return_item; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY return_item (order_id, upc, return_date, quantity) FROM stdin;
\.


--
-- Data for Name: shipping_location; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY shipping_location (shipping_loc_id, loyalty_number, street1, street2, city, state, zip) FROM stdin;
\.


--
-- Name: shipping_location_shipping_loc_id_seq; Type: SEQUENCE SET; Schema: public; Owner: rhol
--

SELECT pg_catalog.setval('shipping_location_shipping_loc_id_seq', 1, false);


--
-- Data for Name: stock; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY stock (store_id, upc, amount, unit_price) FROM stdin;
\.


--
-- Data for Name: store; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY store (store_id, opening_date, street1, street2, city, state, zip, name) FROM stdin;
1	1985-05-11	1100 East Edinger Avenue	\N	Tustin	CA	92780	Orange County/Tustin
2	1995-06-22	8000 East Quincy Avenue	Denver Tech Center	Denver	CO	80237	Denver/Denver Teach Center, Colorado
3	2000-11-13	2340 Pleasant Hill Road	\N	Duluth	GA	30096	Greater Atlanta/Duluth, Georgia
4	2005-03-17	1275 Powers Ferry Rd. SE, Suite 50	Powers Ferry Plaza	Marietta	GA	30067	Gerater Atlanta/Marietta, Georgia
5	2010-10-23	2645 Elston Avenue	\N	Chicago	IL	60647	Chicagoland/Central, Illinois
6	2001-01-23	80 East Ogden Avenue	\N	Chicago	IL	60559	Chicagoland/Westmont, Illinois
7	2009-05-31	9294 Metcalf Avenue	Regency Park Shopping Center	Overland Park	KS	66212	Kansas City/Overland Park, Kansas
8	1994-10-23	730 Memorial Drive	\N	Cambridge	MA	2139	Boston/Cambridge, Massachusetts
9	1991-09-18	1776 E. Jerrerson #203	Federal Plaza	Rockville	MD	20852	Beltway/Rockville, Maryland
10	1989-04-01	1957 E Joppa Road	\N	Parkville	MD	21234	Towson/Baltimore, Maryland
11	1996-08-23	32800 Concord Drive	\N	Madison Heights	MI	48071	Detroit/Madison Heights
12	1999-07-08	3710 Highway 100 South	\N	St. Louis Park	MN	55416	Twin Cities/St. Louis Park
13	1994-11-13	87 Brentwood Promenade Ct	\N	Brentwood	MO	63144	Brentwood, Missouri
14	1998-05-11	263 McLean Blvd.	Route 20 Retail Center	Paterson	NJ	7504	North Jersey/Paterson, New Jersey
15	2007-09-25	655 Merrick Avenue	\N	Westbury	NY	11590	Long Island/Westbury, New York
16	2009-10-15	750-A Central Park Avenue	\N	Yonkers	NY	10704	Westchester County/Yonkers
17	2013-02-21	71-43 Kissena Blvd.	Between 71st and 72nd Avenue	Flushing	NY	113367	Queens/Flushing, New York
18	2014-01-01	850 3rd Avenue	Corner of 31st Street and 3rd Ave.	Brooklyn	NY	11232	Brooklyn/Gowanus Expy
19	1979-05-11	747 Bethel Road	Olentangy Plaza	Columbus	OH	43214	Central Ohio/Columbus
20	1991-03-15	1349 S. O. M. Center Road	Eastgate Shopping Center	Mayfield Heights	OH	44124	Northeast Ohio/Mayfield Heights
21	1980-06-11	11755 Mosteller Road	\N	Sharonville	OH	45241	Cincinnati/Sharonville, Ohio
22	1985-12-01	550 East Lancaster Avenue	St. Davids Square	St. Davids	PA	19087	Philadelphia/St. Davids
23	2001-03-23	1717 West Loop Drive South	\N	Houston	TX	77027	Houston/West Loop, Texas
24	2002-02-01	13292 N. Central Expressway	Keyston Plaza	Dallas	TX	75243	Dallas Metroplex/Richardon, Texas
25	2003-07-17	3089 Nutley Street	Pan Am Plaza	Fairfax	VA	22031	Northern Virginia/Fairfax
\.


--
-- Data for Name: store_closing; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY store_closing (store_id, closed_date, description) FROM stdin;
\.


--
-- Data for Name: store_hours; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY store_hours (store_id, day_of_week, open_hour, close_hour) FROM stdin;
1	M 	10:00:00	21:00:00
1	Tu	10:00:00	21:00:00
1	W 	10:00:00	21:00:00
1	Th	10:00:00	21:00:00
1	F 	10:00:00	21:00:00
1	Sa	10:00:00	21:00:00
1	Su	11:00:00	18:00:00
2	M 	10:00:00	21:00:00
3	M 	10:00:00	21:00:00
4	M 	10:00:00	21:00:00
5	M 	10:00:00	21:00:00
6	M 	10:00:00	21:00:00
7	M 	10:00:00	21:00:00
8	M 	10:00:00	21:00:00
9	M 	10:00:00	21:00:00
10	M 	10:00:00	21:00:00
11	M 	10:00:00	21:00:00
12	M 	10:00:00	21:00:00
13	M 	10:00:00	21:00:00
14	M 	10:00:00	21:00:00
15	M 	10:00:00	21:00:00
16	M 	10:00:00	21:00:00
17	M 	10:00:00	21:00:00
18	M 	10:00:00	21:00:00
19	M 	10:00:00	21:00:00
20	M 	10:00:00	21:00:00
21	M 	10:00:00	21:00:00
22	M 	10:00:00	21:00:00
23	M 	10:00:00	21:00:00
24	M 	10:00:00	21:00:00
25	M 	10:00:00	21:00:00
2	Tu	10:00:00	21:00:00
3	Tu	10:00:00	21:00:00
4	Tu	10:00:00	21:00:00
5	Tu	10:00:00	21:00:00
6	Tu	10:00:00	21:00:00
7	Tu	10:00:00	21:00:00
8	Tu	10:00:00	21:00:00
9	Tu	10:00:00	21:00:00
10	Tu	10:00:00	21:00:00
11	Tu	10:00:00	21:00:00
12	Tu	10:00:00	21:00:00
13	Tu	10:00:00	21:00:00
14	Tu	10:00:00	21:00:00
15	Tu	10:00:00	21:00:00
16	Tu	10:00:00	21:00:00
17	Tu	10:00:00	21:00:00
18	Tu	10:00:00	21:00:00
19	Tu	10:00:00	21:00:00
20	Tu	10:00:00	21:00:00
21	Tu	10:00:00	21:00:00
22	Tu	10:00:00	21:00:00
23	Tu	10:00:00	21:00:00
24	Tu	10:00:00	21:00:00
25	Tu	10:00:00	21:00:00
2	W 	10:00:00	21:00:00
3	W 	10:00:00	21:00:00
4	W 	10:00:00	21:00:00
5	W 	10:00:00	21:00:00
6	W 	10:00:00	21:00:00
7	W 	10:00:00	21:00:00
8	W 	10:00:00	21:00:00
9	W 	10:00:00	21:00:00
10	W 	10:00:00	21:00:00
11	W 	10:00:00	21:00:00
12	W 	10:00:00	21:00:00
13	W 	10:00:00	21:00:00
14	W 	10:00:00	21:00:00
15	W 	10:00:00	21:00:00
16	W 	10:00:00	21:00:00
17	W 	10:00:00	21:00:00
18	W 	10:00:00	21:00:00
19	W 	10:00:00	21:00:00
20	W 	10:00:00	21:00:00
21	W 	10:00:00	21:00:00
22	W 	10:00:00	21:00:00
23	W 	10:00:00	21:00:00
24	W 	10:00:00	21:00:00
25	W 	10:00:00	21:00:00
2	Th	10:00:00	21:00:00
3	Th	10:00:00	21:00:00
4	Th	10:00:00	21:00:00
5	Th	10:00:00	21:00:00
6	Th	10:00:00	21:00:00
7	Th	10:00:00	21:00:00
8	Th	10:00:00	21:00:00
9	Th	10:00:00	21:00:00
10	Th	10:00:00	21:00:00
11	Th	10:00:00	21:00:00
12	Th	10:00:00	21:00:00
13	Th	10:00:00	21:00:00
14	Th	10:00:00	21:00:00
15	Th	10:00:00	21:00:00
16	Th	10:00:00	21:00:00
17	Th	10:00:00	21:00:00
18	Th	10:00:00	21:00:00
19	Th	10:00:00	21:00:00
20	Th	10:00:00	21:00:00
21	Th	10:00:00	21:00:00
22	Th	10:00:00	21:00:00
23	Th	10:00:00	21:00:00
24	Th	10:00:00	21:00:00
25	Th	10:00:00	21:00:00
2	F 	10:00:00	21:00:00
3	F 	10:00:00	21:00:00
4	F 	10:00:00	21:00:00
5	F 	10:00:00	21:00:00
6	F 	10:00:00	21:00:00
7	F 	10:00:00	21:00:00
8	F 	10:00:00	21:00:00
9	F 	10:00:00	21:00:00
10	F 	10:00:00	21:00:00
11	F 	10:00:00	21:00:00
12	F 	10:00:00	21:00:00
13	F 	10:00:00	21:00:00
14	F 	10:00:00	21:00:00
15	F 	10:00:00	21:00:00
16	F 	10:00:00	21:00:00
17	F 	10:00:00	21:00:00
18	F 	10:00:00	21:00:00
19	F 	10:00:00	21:00:00
20	F 	10:00:00	21:00:00
21	F 	10:00:00	21:00:00
22	F 	10:00:00	21:00:00
23	F 	10:00:00	21:00:00
24	F 	10:00:00	21:00:00
25	F 	10:00:00	21:00:00
2	Sa	10:00:00	21:00:00
3	Sa	10:00:00	21:00:00
4	Sa	10:00:00	21:00:00
5	Sa	10:00:00	21:00:00
6	Sa	10:00:00	21:00:00
7	Sa	10:00:00	21:00:00
8	Sa	10:00:00	21:00:00
9	Sa	10:00:00	21:00:00
10	Sa	10:00:00	21:00:00
11	Sa	10:00:00	21:00:00
12	Sa	10:00:00	21:00:00
13	Sa	10:00:00	21:00:00
14	Sa	10:00:00	21:00:00
15	Sa	10:00:00	21:00:00
16	Sa	10:00:00	21:00:00
17	Sa	10:00:00	21:00:00
18	Sa	10:00:00	21:00:00
19	Sa	10:00:00	21:00:00
20	Sa	10:00:00	21:00:00
21	Sa	10:00:00	21:00:00
22	Sa	10:00:00	21:00:00
23	Sa	10:00:00	21:00:00
24	Sa	10:00:00	21:00:00
25	Sa	10:00:00	21:00:00
2	Su	11:00:00	18:00:00
3	Su	11:00:00	18:00:00
4	Su	11:00:00	18:00:00
5	Su	11:00:00	18:00:00
6	Su	11:00:00	18:00:00
7	Su	11:00:00	18:00:00
8	Su	11:00:00	18:00:00
9	Su	11:00:00	18:00:00
10	Su	11:00:00	18:00:00
11	Su	11:00:00	18:00:00
12	Su	11:00:00	18:00:00
13	Su	11:00:00	18:00:00
14	Su	11:00:00	18:00:00
15	Su	11:00:00	18:00:00
16	Su	11:00:00	18:00:00
17	Su	11:00:00	18:00:00
18	Su	11:00:00	18:00:00
19	Su	11:00:00	18:00:00
20	Su	11:00:00	18:00:00
21	Su	11:00:00	18:00:00
22	Su	11:00:00	18:00:00
23	Su	11:00:00	18:00:00
24	Su	11:00:00	18:00:00
25	Su	11:00:00	18:00:00
\.


--
-- Data for Name: store_phone; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY store_phone (store_id, phone) FROM stdin;
1	(714)566-8500
2	(303)302-8500
3	(770)689-2540
4	(770)859-1540
5	(773)292-1700
6	(630)371-5500
7	(913)652-6000
8	(617)234-6400
9	(301)692-2130
10	(410)513-0590
11	(248)291-8400
12	(952)285-4040
13	(314)252-3961
14	(973)653-2187
15	(516)683-6760
16	(914)595-3020
17	(718)674-8400
18	(347)563-9880
19	(614)326-8500
20	(440)449-7000
21	(513)782-8500
22	(610)989-8400
23	(713)940-8500
24	(972)664-8500
25	(703)204-8400
\.


--
-- Name: store_store_id_seq; Type: SEQUENCE SET; Schema: public; Owner: rhol
--

SELECT pg_catalog.setval('store_store_id_seq', 25, true);


--
-- Data for Name: supplies; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY supplies (vendor_name, brand_name) FROM stdin;
\.


--
-- Data for Name: vendor; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY vendor (name, street1, street2, city, state, zip) FROM stdin;
\.


--
-- Data for Name: vendor_phone; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY vendor_phone (vendor_name, phone) FROM stdin;
\.


--
-- Data for Name: vendor_purchase; Type: TABLE DATA; Schema: public; Owner: rhol
--

COPY vendor_purchase (store_id, vendor_name, upc, purchase_date, amount, unit_price) FROM stdin;
\.


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

