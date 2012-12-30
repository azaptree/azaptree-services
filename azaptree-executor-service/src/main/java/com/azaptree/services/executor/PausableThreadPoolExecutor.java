package com.azaptree.services.executor;

public interface PausableThreadPoolExecutor {

	boolean isPaused();

	/**
	 * Pause execution of tasks
	 */
	void pause();

	/**
	 * Resume execution of tasks
	 * 
	 */
	void resume();
}
