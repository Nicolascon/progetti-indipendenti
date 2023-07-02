package com.generation.organization.model.repository;

import java.io.IOException;
import java.lang.reflect.Method;

import com.generation.library.FileHelper;
import com.generation.library.List;
import com.generation.organization.model.entities.Entity;
import com.generation.organization.model.entities.person.Contact;

public class CSVRepository<X extends Entity> 
{
	private String filename;
	private Class entityClass;
	private List<X> content = new List<X>();
	private String propriety;
	
	public CSVRepository(String filename, Class entityClass) throws Exception
	{
		this.filename = filename;
		this.entityClass = entityClass;
		
		String[] rows = FileHelper.readRows(filename);
		// prima riga... fondamentale.. mi serve per i campi
		String[] fields = rows[0].split(",");
		propriety = (rows[0]);
		for(int i=1;i<rows.length;i++)
			content.add(_rowToX(rows[i], fields));
	}

	private X _rowToX(String row, String[] fields) throws Exception
	{
		String[] values = row.split(",");
		Object o = entityClass.newInstance();
		
		for(int i=0;i<fields.length;i++)
		{
			String field = fields[i];
			String setter = "set"+field;
			Method m = entityClass.getMethod(setter, String.class);
			m.invoke(o, values[i]);
		}
		return (X) o;
	}
	
	private String _XToRow(X x) throws Exception
	{
		String res = "";
		String[] values = propriety.split(",");
		for(int i=0;i<values.length;i++)
		{
			String field = values[i];
			String getter = "get"+field;
			Method m = entityClass.getMethod(getter);
			res+=m.invoke(x);
			if(i<values.length-1)
				res+=",";
		}
		return res;
	}
	
	public List<X> getAll()
	{
		return content;
	}
	
	public X get(String ID)
	{
		for(X x:content)
			if(x.getID().equals(ID))
				return x;
		
		return null;
	}

	public void insert(X x) throws Exception
	{
		for(X c:content)
		if(c.getID().equals(x.getID()))
			throw new RuntimeException("ID già presente");
		
		content.add(x);
		_sync();
		
	}
	
	private void _sync() throws Exception
	{
		String newFileContent = propriety+"\n";
		for(int i=0;i<content.size();i++)
		{
			newFileContent+=_XToRow(content.get(i));
			if(i<content.size()-1)
				newFileContent+="\n";
			FileHelper.writeString(filename, newFileContent);
		}
	}
	
}
