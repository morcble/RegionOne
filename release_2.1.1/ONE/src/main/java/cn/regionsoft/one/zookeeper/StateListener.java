package cn.regionsoft.one.zookeeper;


public interface StateListener {
	void stateChanged(final StateListener.State state);
	
	public enum State {
		DISCONNECTED(0), CONNECTED(1), RECONNECTED(2), ;
		int code;

		private State(int code) {
			this.code = code;
		}

		public int code() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}
	}

}
