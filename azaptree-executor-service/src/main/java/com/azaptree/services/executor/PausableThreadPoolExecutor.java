package com.azaptree.services.executor;

public interface PausableThreadPoolExecutor {

	boolean isPaused();

	/**
	 * Pause execution of tasks. This only affects tasks that are submitted after the executor has been paused.
	 * 
	 * Tasks that are currently running will continue.
	 */
	void pause();

	/**
	 * Resume execution of tasks
	 * 
	 */
	void resume();
}
