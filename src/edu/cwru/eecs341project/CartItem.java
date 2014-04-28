package edu.cwru.eecs341project;

public class CartItem {
		public final Long upc;
		public final int storeId;
		public final String storeName;
		public final String name;
		private int quantity;
		public final double price;
		
		public CartItem(Long upc, int storeId, String storeName, String name, int quantity, double price) throws Exception
		{
			this.upc = upc;
			this.storeId = storeId;
			this.storeName = storeName;
			this.name = name;
			if(quantity < 0)
				throw new Exception("quantity must be >= 0");
			this.quantity = quantity;
			if(price < 0)
				throw new Exception("price must be >= 0");
			this.price = price;
		}
		
		public int getQuantity()
		{
			return quantity;
		}
		
		public void setQuantity(int quantity) throws Exception
		{
			if(quantity < 0)
				throw new Exception("quantity must be >= 0");
			this.quantity = quantity;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof CartItem)
			{
				CartItem j = (CartItem)obj;
				if(this.upc != j.upc)
					return false;
				if(this.storeId != j.storeId)
					return false;
				return true;
			}
			return false;
		}
	}