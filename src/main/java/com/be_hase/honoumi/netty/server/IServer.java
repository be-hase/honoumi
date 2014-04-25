package com.be_hase.honoumi.netty.server;

import java.nio.charset.Charset;

import org.jboss.netty.bootstrap.ServerBootstrap;

import com.be_hase.honoumi.routing.Router;
import com.google.inject.Injector;

public interface IServer {
	/**
	 * get server name<br>
	 * <br>
	 * @return
	 */
	public String getServerName();
	
	/**
	 * get server bootstrap<br>
	 * <br>
	 * @return
	 */
	public ServerBootstrap getServerBootstrap();
	
	/**
	 * get injector<br>
	 * <br>
	 * @return
	 */
	public Injector getInjector();
	
	/**
	 * get charset(string)<br>
	 * <br>
	 * @return
	 */
	public String getCharsetStr();
	
	/**
	 * get charset<br>
	 * <br>
	 * @return
	 */
	public Charset getCharset();
	
	/**
	 * get binding port<br>
	 * <br>
	 * @return
	 */
	public int getPort();
	
	/**
	 * get router<br>
	 * <br>
	 * @return
	 */
	public Router getRouter();
	
	/**
	 * isSuppportKeepAlive<br>
	 * <br>
	 * @return
	 */
	public boolean isSuppportKeepAlive();
	
	/**
	 * isSupportChunkAggregate<br>
	 * <br>
	 * @return
	 */
	public boolean isSupportChunkAggregate();
	
	/**
	 * getChunkAggregateMaxContentLength<br>
	 * <br>
	 * @return
	 */
	public int getChunkAggregateMaxContentLength();
	
	/**
	 * isSupportContentCompress<br>
	 * <br>
	 * @return
	 */
	public boolean isSupportContentCompress();
	
	
	
	/**
	 * start server<br>
	 * <br>
	 */
	public void start();
	
	/**
	 * stop server<br>
	 * <br>
	 */
	public void stop();
}