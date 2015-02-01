/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.facebook.api.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.social.facebook.api.Account;
import org.springframework.social.facebook.api.FacebookLink;
import org.springframework.social.facebook.api.GraphApi;
import org.springframework.social.facebook.api.Page;
import org.springframework.social.facebook.api.PageAdministrationException;
import org.springframework.social.facebook.api.PageOperations;
import org.springframework.social.facebook.api.PagePicture;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

class PageTemplate extends AbstractFacebookOperations implements PageOperations {

	private final GraphApi graphApi;
	private final RestTemplate restTemplate;

	public PageTemplate(GraphApi graphApi, RestTemplate restTemplate, boolean isAuthorizedForUser) {
		super(isAuthorizedForUser);
		this.graphApi = graphApi;
		this.restTemplate = restTemplate;
	}

	public Page getPage(String pageId) {
		return graphApi.fetchObject(pageId, Page.class);
	}
	
	public PagePicture getPagePicture(String pageId) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
		queryParams.put("redirect", Arrays.asList("false"));
		
		return graphApi.fetchObjectSuffix(pageId, "picture", PagePicture.class, queryParams);
	}

	public boolean isPageAdmin(String pageId) {
		requireAuthorization();
		return getAccount(pageId) != null;
	}
	
	public PagedList<Account> getAccounts() {
		requireAuthorization();
		return graphApi.fetchConnections("me", "accounts", Account.class);
	}

	public String post(String pageId, String message) {
		requireAuthorization();
		String pageAccessToken = getPageAccessToken(pageId);
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.set("message", message);
		map.set("access_token", pageAccessToken);
		return graphApi.publish(pageId, "feed", map);
	}
	
	public String post(String pageId, String message, FacebookLink link) {
		requireAuthorization();
		String pageAccessToken = getPageAccessToken(pageId);
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.set("link", link.getLink());
		map.set("name", link.getName());
		map.set("caption", link.getCaption());
		map.set("description", link.getDescription());
		map.set("message", message);
		map.set("access_token", pageAccessToken);
		return graphApi.publish(pageId, "feed", map);
	}

	public String postPhoto(String pageId, String albumId, Resource photo) {
		return postPhoto(pageId, albumId, photo, null);
	}
	
	public String postPhoto(String pageId, String albumId, Resource photo, String caption) {
		requireAuthorization();
		String pageAccessToken = getPageAccessToken(pageId);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.set("source", photo);
		if(caption != null) {
			parts.set("message", caption);
		}
		parts.set("access_token", pageAccessToken);
		return graphApi.publish(albumId, "photos", parts);
	}
	

	
	@SuppressWarnings("unchecked")
	public String postVideo(String pageId, Resource video, String title, String description) {
		requireAuthorization();
		String pageAccessToken = getPageAccessToken(pageId);
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.set("source", video);
		
		if(!StringUtils.isEmpty(title))
			parts.set("title", title);
		
		if(!StringUtils.isEmpty(description))
			parts.set("description", description);
		
		parts.set("access_token", pageAccessToken);
		
		Map<String, Object> response = restTemplate.postForObject("https://graph-video.facebook.com/" + pageId + "/videos", parts, Map.class);
		return (String) response.get("id");
	}
	
	public String postPhoto(String pageId, Resource photo, String caption) {
		requireAuthorization();
		
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		String pageAccessToken = getPageAccessToken(pageId);
		parts.set("access_token", pageAccessToken);
		
		if(photo != null) {
			parts.set("source", photo);
		}
		
		if(caption != null) {
			parts.set("message", caption);
		}
		
		return graphApi.publish(pageId, "photos", parts);
	}
	
	public PagedList<Page> search(String query) {
		requireAuthorization();
		MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<String, String>();
		queryMap.add("q", query);
		return graphApi.fetchConnections("search", null, Page.class, queryMap);
	}
	
	public PagedList<Page> search(String query, double latitude, double longitude, long distance) {
		requireAuthorization();
		MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<String, String>();
		queryMap.add("q", query);
		queryMap.add("type", "place");
		queryMap.add("center", latitude + "," + longitude);
		queryMap.add("distance", String.valueOf(distance));
		return graphApi.fetchConnections("search", null, Page.class, queryMap);
	}

	// private helper methods
	
	private Map<String, Account> accountCache = new HashMap<String, Account>();
	
	private String getPageAccessToken(String pageId) {
		Account account = getAccount(pageId);
		if(account == null) {
			throw new PageAdministrationException(pageId);
		}
		return account.getAccessToken();
	}
	
	private Account getAccount(String pageId) {
		if(!accountCache.containsKey(pageId)) {
			// only bother fetching the account data in the event of a cache miss
			List<Account> accounts = getAccounts();
			for (Account account : accounts) {
				accountCache.put(account.getId(), account);
			}
		}
		return accountCache.get(pageId);
	}
}
