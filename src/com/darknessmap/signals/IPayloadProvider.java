package com.darknessmap.signals;

import com.paulm.jsignal.Signal;

public interface IPayloadProvider {
	
	public Signal onPayloadReady();
}
