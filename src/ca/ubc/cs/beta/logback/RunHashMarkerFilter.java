package ca.ubc.cs.beta.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;


public class RunHashMarkerFilter extends Filter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if(!event.getMarker().getName().equals("RUN_HASH"))
		{
			return FilterReply.DENY;
		} else
		{
			return FilterReply.ACCEPT;
		}
	}

}
