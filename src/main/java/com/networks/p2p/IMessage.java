package com.networks.p2p;

import java.io.Serializable;

public interface IMessage extends Serializable {
	MessageType getType();
}
