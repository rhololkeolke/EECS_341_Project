package edu.cwru.eecs341project;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

	private List<CartItem> items;
	
	public ShoppingCart() {
		items = new ArrayList<CartItem>();
	}
	
	public List<CartItem> getItems()
	{
		return items;
	}
	
	public CartItem getItem(Long upc)
	{
		for(int i=0; i<items.size(); i++)
		{
			if(items.get(i).equals(upc))
				return items.get(i);
		}
		return null;
	}
	
	public void addItem(CartItem item) throws Exception
	{
		if(items.contains(item))
		{
			int i = items.indexOf(item);
			items.get(i).setQuantity(items.get(i).getQuantity() + item.getQuantity());
		}
		else
		{
			items.add(item);
		}
	}
	
	public void removeItem(CartItem item)
	{
		items.remove(item);
	}
}
