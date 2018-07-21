package com.kavi.common.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;


import org.bson.Document;

public class LDAPUtility {

	private static LinkedHashMap<String, String> mapMatchGroups = new LinkedHashMap<String, String>();

	private final static Logger logger = Logger.getLogger(LDAPUtility.class);
	private static Properties prop = new Properties(); 
	
	private static String url="";
	private static String domainName="";
	private static String directoryName="";
	private static String password="";
	private static String ldapNSLookupURL="";
	private static String ldapNSlookupdomainName="";
	
	public static String getUrl() {
		return url;
	}
	public static void setUrl(String url) {
		LDAPUtility.url = url;
	}
	public static String getDomainName() {
		return domainName;
	}
	public static void setDomainName(String domainName) {
		LDAPUtility.domainName = domainName;
	}
	public static String getDirectoryName() {
		return directoryName;
	}
	public static void setDirectoryName(String directoryName) {
		LDAPUtility.directoryName = directoryName;
	}
	public static String getPassword() {
		return password;
	}
	public static void setPassword(String password) {
		LDAPUtility.password = password;
	}
	public static String getLdapNSLookupURL() {
		return ldapNSLookupURL;
	}
	public static void setLdapNSLookupURL(String ldapNSLookupURL) {
		LDAPUtility.ldapNSLookupURL = ldapNSLookupURL;
	}
	public static void initializeLDAP(){
		String file = "/environment.properties";
		if(prop.getProperty("LDAP_URL")==null) {
	   		InputStream inputStream = LDAPUtility.class.getResourceAsStream(file); 
	   		Reader reader = new InputStreamReader(inputStream);
			try {
				prop.load(reader);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				reader.close();
				inputStream.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			url = prop.getProperty("LDAP_URL");
			domainName=prop.getProperty("LDAP_DOMAIN_NAME");
			directoryName=prop.getProperty("LDAP_DIRECTORY_NAME");
			password=prop.getProperty("LDAP_PASSWORD");
			ldapNSLookupURL=prop.getProperty("LDAP_NS_LOOKUP_URL");
			ldapNSlookupdomainName=prop.getProperty("LDAP_NS_DOMAIN_NAME");
		}
	}
	/**
	 * 
	 * @return Context of LDAP
	 */
	public static DirContext getLDAPContext() {
		initializeLDAP();
		DirContext ldapContext = null;
		try {
			
			String conn_type = "simple";
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, url);
			environment.put(Context.SECURITY_AUTHENTICATION, conn_type);
			environment.put(Context.SECURITY_PRINCIPAL, domainName);
			environment.put(Context.SECURITY_CREDENTIALS, password);
			environment.put("java.naming.ldap.attributes.binary", "tokenGroups");
			ldapContext = new InitialDirContext(environment);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return ldapContext;
	}

	public static Map<String, String> getGroupNames(String searchFilter, String sOrganizationUnit,
			DirContext ldapContext) {

		Map<String, String> mapGroups = new HashMap<String, String>();
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		try {
			NamingEnumeration userAnswer = ldapContext.search(searchFilter, sOrganizationUnit, searchControls);
			while (userAnswer.hasMoreElements()) {
				SearchResult sr = (SearchResult) userAnswer.next();
				Attributes attrs = sr.getAttributes();
				String value = (String) attrs.get("ou").get();
				mapGroups.put(value, value);
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mapGroups;

	}

	public static void getAllGroupsForUser(DirContext ctx, String nm, String searchUser) throws NamingException {
		mapMatchGroups = new LinkedHashMap<String, String>();
		getAllGroupsParent(ctx, nm, searchUser);

	}
	
	/**
	 * 
	 * @param organizationName
	 * @return
	 */
	public static JSONArray getAllUsersForSRVOrganization(String organizationName) {
		JSONArray userArray = new JSONArray();
		try {
			DirContext ldapContext = getSRVLDAPContext();
			if (ldapContext != null) {
				String filter = "(&(objectClass=person))";
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "uid", "entryDN" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = ldapContext.search(
						"o=" + organizationName + "," + directoryName, filter, groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					StringTokenizer st = new StringTokenizer((String) attrs.get("uid").get(), ",");
					while (st.hasMoreElements()) {
						String uid = st.nextToken();
						userArray.add(uid);
					}
				}
			}
		} catch (Exception exception) {
			userArray = new JSONArray();
		}
		return userArray;
	}
	
	/**
	 * 
	 * @param organizationName
	 * @return
	 */
	public static JSONArray getAllUsersContactForSRVOrganization(String organizationName) {
		JSONArray fieldarray = new JSONArray();
		try {
			DirContext ldapContext = getSRVLDAPContext();
			if (ldapContext != null) {
				String filter = "(&(objectClass=person))";
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "uid", "entryDN", "displayName", "mail", "mobile" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = ldapContext.search(
						"o=" + organizationName + "," + directoryName, filter, groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					String user_id = "";
					String display_name = "";
					String mail = "";
					String mobile = "";
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					if (attrs.get("uid") != null) {
					user_id = (String) attrs.get("uid").get();
					}
					if (attrs.get("displayName") != null) {
						display_name = (String) attrs.get("displayName").get();
					}
					if (attrs.get("mail") != null) {
						mail = (String) attrs.get("mail").get();
					}
					if (attrs.get("mobile") != null) {
						mobile = (String) attrs.get("mobile").get();
					}
					Document doc = new Document();
					doc.put("user_id", user_id);
					doc.put("display_name", display_name);
					doc.put("user_email", mail);
					doc.put("phone", mobile);
					fieldarray.add(doc);
				}

			}

		} catch (Exception exception) {
			fieldarray = new JSONArray();
		}
		return fieldarray;
	}
	
	
	/**
	 * 
	 * @param organizationName
	 * @return
	 */
	public static JSONArray getAllUsersForOrganization(String organizationName) {
		initializeLDAP();
		JSONArray userArray = new JSONArray();
		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, domainName);
		environment.put(Context.SECURITY_CREDENTIALS, password);
		environment.put("java.naming.ldap.attributes.binary", "tokenGroups");
		try {
			DirContext ldapContext = new InitialDirContext(environment);
			if (ldapContext != null) {
				String filter = "(&(objectClass=person))";
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "uid", "entryDN" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = ldapContext.search(
						"o=" + organizationName + "," + directoryName, filter, groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					StringTokenizer st = new StringTokenizer((String) attrs.get("uid").get(), ",");
					while (st.hasMoreElements()) {
						String uid = st.nextToken();
						userArray.add(uid);
					}
				}
			}
		} catch (Exception exception) {
			userArray = new JSONArray();
		}
		return userArray;
	}
	
	/**
	 * 
	 * @param organizationName
	 * @return
	 */
	public static JSONArray getAllSRVRoles(String organizationName) {
		JSONArray roleArray = new JSONArray();
		try {
			DirContext ldapContext = getSRVLDAPContext();
			if (ldapContext != null) {
				String filter = "(&(objectClass=organizationalRole))";
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "uid", "entryDN", "cn" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = ldapContext.search(
						"o=" + organizationName + "," + directoryName, filter, groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					StringTokenizer st = new StringTokenizer((String) attrs.get("cn").get(), ",");
					while (st.hasMoreElements()) {
						String uid = st.nextToken();
						roleArray.add(uid);
					}
				}
			}
		} catch (Exception exception) {
			roleArray = new JSONArray();
		}
		return roleArray;
	}
	

	/**
	 * 
	 * @param organizationName
	 * @return
	 */
	public static JSONArray getAllRoles(String organizationName) {
		initializeLDAP();
		JSONArray roleArray = new JSONArray();
		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, domainName);
		environment.put(Context.SECURITY_CREDENTIALS, password);
		environment.put("java.naming.ldap.attributes.binary", "tokenGroups");
		try {
			DirContext ldapContext = new InitialDirContext(environment);
			if (ldapContext != null) {
				String filter = "(&(objectClass=organizationalRole))";
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "uid", "entryDN", "cn" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = ldapContext.search(
						"o=" + organizationName + "," + directoryName, filter, groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					StringTokenizer st = new StringTokenizer((String) attrs.get("cn").get(), ",");
					while (st.hasMoreElements()) {
						String uid = st.nextToken();
						roleArray.add(uid);
					}
				}
			}
		} catch (Exception exception) {
			roleArray = new JSONArray();
		}
		return roleArray;
	}

	/**
	 * 
	 * @param ctx
	 * @param nm
	 * @param searchUser
	 * @throws NamingException
	 */
	public static void getAllGroupsParent(DirContext ctx, String nm, String searchUser) throws NamingException {
		NamingEnumeration<NameClassPair> contentsEnum = ctx.list(nm);
		while (contentsEnum.hasMore()) {
			NameClassPair ncp = (NameClassPair) contentsEnum.next();
			String userName = ncp.getName();
			Attributes attr1 = ctx.getAttributes(userName + "," + nm, new String[] { "objectClass", "ou" });
			if (attr1.get("objectClass").toString().indexOf("CN=Person") == -1) {
				// Recurse sub-contexts
				String value = (String) attr1.get("ou").get();
				if (searchUser.equalsIgnoreCase(value)) {
					getGroupName(nm, ctx, searchUser);
				}
				getAllGroupsParent(ctx, userName + "," + nm, searchUser);
			}
		}
	}

	/**
	 * 
	 * @param nm
	 * @param ctx
	 *            If we wantall groups print the name and check
	 * @return
	 */
	private static void getGroupName(String nm, DirContext ctx, String searchuser) {
		String groupName = "";
		StringTokenizer st = new StringTokenizer(nm, ",");
		while (st.hasMoreElements()) {
			String name = st.nextToken();
			if (name.startsWith("ou")) {
				groupName = name.substring(3, name.length());
				mapMatchGroups.put(groupName, searchuser);
			}
		}
	}

	/**
	 * 
	 * @param nm
	 * @param ctx
	 *            To display corressponding groups
	 * @return
	 */
	/*
	 * private static String getGroupName(String nm,DirContext ctx){ Attributes
	 * attr1; String groupName=""; try { attr1 = ctx.getAttributes(nm);
	 * groupName =(String) attr1.get("ou").get(); } catch (NamingException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * 
	 * return groupName; }
	 */
	public static LinkedHashMap<String, String> getMapMatchGroups() {
		return mapMatchGroups;
	}

	public static void setMapMatchGroups(LinkedHashMap<String, String> mapMatchGroups) {
		LDAPUtility.mapMatchGroups = mapMatchGroups;
	}

	public static void main(String args[]) {

		// LDAPUtility.getAllUsersForGroup("cn=Manager,dc=kaviglobal,dc=com",CommonConstants.LDAP_URL,"plexa_administrators",CommonConstants.LDAP_DIRECTORY_NAME);
	}

	/**
	 * 
	 * @param organizationName
	 * @return
	 */
	public static JSONArray getAllUsersContactForOrganization(String organizationName) {
		initializeLDAP();
		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, domainName);
		environment.put(Context.SECURITY_CREDENTIALS, password);
		environment.put("java.naming.ldap.attributes.binary", "tokenGroups");
		JSONArray fieldarray = new JSONArray();
		try {
			DirContext ldapContext = new InitialDirContext(environment);
			if (ldapContext != null) {
				String filter = "(&(objectClass=person))";
				SearchControls groupsSearchCtls = new SearchControls();
				groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String sRetAttr[] = { "uid", "entryDN", "displayName", "mail", "mobile" };
				groupsSearchCtls.setReturningAttributes(sRetAttr);
				NamingEnumeration userAnswer = ldapContext.search(
						"o=" + organizationName + "," + directoryName, filter, groupsSearchCtls);
				while (userAnswer.hasMoreElements()) {
					String user_id = "";
					String display_name = "";
					String mail = "";
					String mobile = "";
					SearchResult sr = (SearchResult) userAnswer.next();
					Attributes attrs = sr.getAttributes();
					if (attrs.get("uid") != null) {
					user_id = (String) attrs.get("uid").get();
					}
					if (attrs.get("displayName") != null) {
						display_name = (String) attrs.get("displayName").get();
					}
					if (attrs.get("mail") != null) {
						mail = (String) attrs.get("mail").get();
					}
					if (attrs.get("mobile") != null) {
						mobile = (String) attrs.get("mobile").get();
					}
					Document doc = new Document();
					doc.put("user_id", user_id);
					doc.put("display_name", display_name);
					doc.put("user_email", mail);
					doc.put("phone", mobile);
					fieldarray.add(doc);
				}

			}

		} catch (Exception exception) {
			fieldarray = new JSONArray();
		}
		return fieldarray;
	}
	
	/**
	 * 
	 * @return Context of LDAP
	 */
	public static DirContext getSRVLDAPContext() {
		initializeLDAP();
		String ldapURL=getLDAPURLFromNSLookup(ldapNSLookupURL);
		DirContext ldapContext = null;
		try {
			
			String conn_type = "simple";
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
			environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			environment.put(Context.PROVIDER_URL, ldapURL);
			environment.put(Context.SECURITY_AUTHENTICATION, conn_type);
			environment.put(Context.SECURITY_PRINCIPAL, ldapNSlookupdomainName);
			environment.put(Context.SECURITY_CREDENTIALS, password);
			environment.put("java.naming.ldap.attributes.binary", "tokenGroups");
			ldapContext = new InitialDirContext(environment);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return ldapContext;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private static String getLDAPURLFromNSLookup(String ldapNSLookupURL) {
	
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		env.put("java.naming.provider.url", "dns:");
		try {
			DirContext ctx = new InitialDirContext(env);
			Attributes attributes = ctx.getAttributes(ldapNSLookupURL, new String[]{"SRV"});
			Attribute attr = attributes.get("SRV");
			String str="";
			 for (NamingEnumeration e = attr.getAll(); e.hasMore();){
			   str=e.next().toString();
			 }
			 String[] aRecordDetails=str.split("\\s+");
			 if(aRecordDetails.length>0){
				 String ldapURL="ldap://"+aRecordDetails[3]+":"+aRecordDetails[2];
			 	 return ldapURL; 
			 }
			ctx.close();
			return "";
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	public static String getLdapNSlookupdomainName() {
		return ldapNSlookupdomainName;
	}
	public static void setLdapNSlookupdomainName(String ldapNSlookupdomainName) {
		LDAPUtility.ldapNSlookupdomainName = ldapNSlookupdomainName;
	}
	
}
