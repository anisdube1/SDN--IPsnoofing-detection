package net.floodlightcontroller.MySimpleSwitch;

import net.floodlightcontroller.core.IFloodlightProviderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

import net.floodlightcontroller.learningswitch.LearningSwitch;
import net.floodlightcontroller.packet.Ethernet;

import org.openflow.util.HexString;
import org.openflow.util.LRULinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.net.InetAddress;

import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.RoutingDecision;
import net.floodlightcontroller.counter.ICounterStoreService;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.util.U16;

import java.util.Collection;
import java.util.Collections;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.Wildcards;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.MacVlanPair;
import net.floodlightcontroller.counter.ICounterStoreService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.firewall.FirewallRule;
import net.floodlightcontroller.firewall.RuleWildcardsPair;
import net.floodlightcontroller.packet.BasePacket;

import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;

import java.util.ArrayList;

import net.floodlightcontroller.packet.IPv4;

import java.util.Arrays;

import net.floodlightcontroller.packet.Ethernet;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.RoutingDecision;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

public class MySimpleSwitch implements IOFMessageListener, IFloodlightModule {
	
	

	protected IFloodlightProviderService floodlightProvider;
	protected ICounterStoreService counterStore;
	protected IStaticFlowEntryPusherService staticFlowEntryPusher;
	protected Set macAddresses;
	protected static Logger logger;
	protected Map<Long, Short> macToPort;
	protected Map<String , Short> ipToPort = new HashMap<String,Short>();
    protected Map<IOFSwitch, Map<String,Short>> sw_ip_port = new HashMap<IOFSwitch , Map<String , Short>>();
    protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 100; // in seconds
    protected static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
    protected String dest_ip  , src_ip , mask ;
    protected static final boolean LEARNING_SWITCH_REVERSE_FLOW = true ;
    
    int flag ;
    
	

	@Override
	public String getName() {
		return MySimpleSwitch.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		
		System.out.println("here i am ");

		
		Collection<Class<? extends IFloodlightService>> l =
		        new ArrayList<Class<? extends IFloodlightService>>();
		    l.add(IFloodlightProviderService.class);
		    return l;
		
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		
		System.out.println("here i am 3");

		
		 floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		  counterStore =
	                context.getServiceImpl(ICounterStoreService.class);

		    macAddresses = new ConcurrentSkipListSet<Long>();
		    macToPort 		   = new HashMap<Long, Short>();
		    logger = LoggerFactory.getLogger(MySimpleSwitch.class);
		
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {

		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

	}
	
	
	 private void writePacketOutForPacketIn(IOFSwitch sw,
             OFPacketIn packetInMessage,
             short egressPort) {
OFPacketOut packetOutMessage = (OFPacketOut) floodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT);
short packetOutLength = (short)OFPacketOut.MINIMUM_LENGTH; // starting length

// Set buffer_id, in_port, actions_len
packetOutMessage.setBufferId(packetInMessage.getBufferId());
packetOutMessage.setInPort(packetInMessage.getInPort());
packetOutMessage.setActionsLength((short)OFActionOutput.MINIMUM_LENGTH);
packetOutLength += OFActionOutput.MINIMUM_LENGTH;

// set actions
List<OFAction> actions = new ArrayList<OFAction>(1);
actions.add(new OFActionOutput(egressPort, (short) 0));
packetOutMessage.setActions(actions);

// set data - only if buffer_id == -1
if (packetInMessage.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
byte[] packetData = packetInMessage.getPacketData();
packetOutMessage.setPacketData(packetData);
packetOutLength += (short)packetData.length;
}

// finally, set the total length
packetOutMessage.setLength(packetOutLength);

// and write it out
try {
counterStore.updatePktOutFMCounterStoreLocal(sw, packetOutMessage);
sw.write(packetOutMessage, null);
} catch (IOException e) {

}
}
	
	/*
	 * control logic which install static rules 
	 * */
	private Command ctrlLogicWithRules(IOFSwitch sw, OFPacketIn pi) {
		
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), pi.getInPort());		
        
		// take the source and destination mac from the packet
		Long sourceMac = Ethernet.toLong(match.getDataLayerSource());
        Long destMac   = Ethernet.toLong(match.getDataLayerDestination());
	 	  dest_ip = IPv4.fromIPv4Address(match.getNetworkDestination());
	     src_ip = IPv4.fromIPv4Address(match.getNetworkSource());
	 
        Short inputPort = pi.getInPort();
     
        long p = sw.getId();
        System.out.println("Switch id : "); 
        System.out.println(p);
	 	System.out.println("Get the Desitnation IP Address"); 
	 	System.out.println(IPv4.fromIPv4Address(match.getNetworkDestination()));
	 	System.out.println("Get the Source IP Address"); 
	 	System.out.println(IPv4.fromIPv4Address(match.getNetworkSource()));
	 	
	 	
        
        Map<String , Short> ipwithport = sw_ip_port.get(sw); 
	 
        if(ipwithport == null)
        {
         ipwithport = Collections.synchronizedMap(new LRULinkedHashMap<String,Short>(1000));
         sw_ip_port.put(sw, ipwithport);
         
       }

      
   	
       if (!(ipwithport.containsKey(src_ip))) 
       { 	 
         ipwithport.put(src_ip ,  inputPort);
         sw_ip_port.put(sw , ipwithport);
         System.out.println("If map does not contain src_ip mapping : then add the corresponding port ");
         System.out.println("ipwithport keys : "+ ipwithport.keySet());
         System.out.println("ipwithport valuess : "+ ipwithport.values());
         System.out.println("Source IP where port is  stored ");
         System.out.println(inputPort);
       }
       
       
        Short outPort = ipwithport.get(dest_ip);

    
        if (outPort == null) 
        {
        	this.writePacketOutForPacketIn(sw, pi, OFPort.OFPP_FLOOD.getValue());  
             	
        }	
        else {

        	OFFlowMod rule = new OFFlowMod();
 			rule.setType(OFType.FLOW_MOD); 		
 			rule.setCommand(OFFlowMod.OFPFC_ADD);
 			match.setWildcards(~OFMatch.OFPFW_DL_DST);
 			rule.setMatch(match);
 			
 			// specify timers for the life of the rule
 			rule.setIdleTimeout(MySimpleSwitch.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
 			rule.setHardTimeout(MySimpleSwitch.FLOWMOD_DEFAULT_HARD_TIMEOUT);
 	        
 	        // set the buffer id to NONE - implementation artifact
 			rule.setBufferId(OFPacketOut.BUFFER_ID_NONE);
 	       
 	        // set of actions to apply to this rule
 			ArrayList<OFAction> actions = new ArrayList<OFAction>();
 			OFAction outputTo = new OFActionOutput(outPort);
 			
 			actions.add(outputTo);
 			rule.setActions(actions);
 			 			
 			// specify the length of the flow structure created
 			rule.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH)); 			
 				
 			logger.debug("install rule for destination {}", destMac);
 			
 			try {
 				sw.write(rule, null);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}	

 	   
 			match.setNetworkSource(IPv4.toIPv4Address(src_ip));
 			match.setNetworkDestination(IPv4.toIPv4Address(dest_ip)); 
 			match.setWildcards(Wildcards.FULL.withNwSrcMask(8).withNwDstMask(8));    
 			
 			
 			int SHIFT_BITS = 9;
 			int nw_dst_mask = ((1 << OFMatch.OFPFW_NW_DST_BITS) - 1) << SHIFT_BITS;
 			int nw_src_mask = ((1 << OFMatch.OFPFW_NW_SRC_BITS) - 1) << SHIFT_BITS;

 			
 			match.setWildcards(Wildcards.FULL.matchOn(Flag.IN_PORT).matchOn(Flag.DL_TYPE).withNwSrcMask(32).withNwDstMask(32));

        	this.pushPacket(sw, match, pi, outPort);  
        	this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, OFPacketOut.BUFFER_ID_NONE, match, outPort);
        	 if (LEARNING_SWITCH_REVERSE_FLOW) {
                 this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, -1, match.clone()
                     .setDataLayerSource(match.getDataLayerDestination())
                     .setDataLayerDestination(match.getDataLayerSource())
                     .setNetworkSource(match.getNetworkDestination())
                     .setNetworkDestination(match.getNetworkSource())
                     .setTransportSource(match.getTransportDestination())
                     .setTransportDestination(match.getTransportSource())
                     .setInputPort(outPort),
                     match.getInputPort());
             }
        }       
        
        return Command.CONTINUE;
	}

	
	
	
	private void writeFlowMod(IOFSwitch sw, short command, int bufferId,
            OFMatch match, short outPort) {

        OFFlowMod flowMod = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
        flowMod.setMatch(match);
        flowMod.setCommand(command);
        flowMod.setIdleTimeout(MySimpleSwitch.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
        flowMod.setHardTimeout(MySimpleSwitch.FLOWMOD_DEFAULT_HARD_TIMEOUT);
        flowMod.setBufferId(bufferId);
        flowMod.setOutPort((command == OFFlowMod.OFPFC_DELETE) ? outPort : OFPort.OFPP_NONE.getValue());
        flowMod.setFlags((command == OFFlowMod.OFPFC_DELETE) ? 0 : (short) (1 << 0)); // OFPFF_SEND_FLOW_REM

        flowMod.setActions(Arrays.asList((OFAction) new OFActionOutput(outPort, (short) 0xffff)));
        flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH));

        counterStore.updatePktOutFMCounterStoreLocal(sw, flowMod);

        // and write it out
        try {
            sw.write(flowMod, null);
        } catch (IOException e) {
            
        }
    }


private void pushPacket(IOFSwitch sw, OFMatch match, OFPacketIn pi, short outport) {
		
		// create an OFPacketOut for the pushed packet
        OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory()
                		.getMessage(OFType.PACKET_OUT);        
        
        // update the inputPort and bufferID
        po.setInPort(pi.getInPort());
        po.setBufferId(pi.getBufferId());
                
        // define the actions to apply for this packet
        OFActionOutput action = new OFActionOutput();
		action.setPort(outport);		
		po.setActions(Collections.singletonList((OFAction)action));
		po.setActionsLength((short)OFActionOutput.MINIMUM_LENGTH);
	        
        // set data if it is included in the packet in but buffer id is NONE
        if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
            byte[] packetData = pi.getPacketData();
            po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                    + po.getActionsLength() + packetData.length));
            po.setPacketData(packetData);
        } else {
            po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                    + po.getActionsLength()));
        }        
        
        // push the packet to the switch
        try {
            sw.write(po, null);
        } catch (IOException e) {
            logger.error("failed to write packetOut: ", e);
        }
	}



	@Override
	   public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
	
		 BasePacket pkt = (BasePacket) IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		  //Instantiate two objects for OFMatch and OFPacketIn
		 	 OFPacketIn pin = (OFPacketIn) msg;
		 	 OFMatch match = new OFMatch();
		 	 match.loadFromPacket(pin.getPacketData(), pin.getInPort());
		 	 
		 	Long sourceMac = Ethernet.toLong(match.getDataLayerSource());
	        Long destMac   = Ethernet.toLong(match.getDataLayerDestination());
		 	 
		 	 
		 	 String s_mac = HexString.toHexString(sourceMac);
		     String d_mac = HexString.toHexString(destMac);
		 
		    
		        IRoutingDecision decision = null;
		        
		        dest_ip = IPv4.fromIPv4Address(match.getNetworkDestination());
			     src_ip = IPv4.fromIPv4Address(match.getNetworkSource());

		
	        match.loadFromPacket(((OFPacketIn)msg).getPacketData(), 
	        					 ((OFPacketIn)msg).getInPort());
	        
	        		
	        
	        if((match.getDataLayerType() == Ethernet.TYPE_ARP ))
	        {
	    //    System.out.println("PAcket Received ad I am a ARP packet So going to learn the src ip for it if needed ");
			return this.ctrlLogicWithRules(sw, (OFPacketIn) msg);
	        }
	        
	        if (match.getDataLayerType() == Ethernet.TYPE_IPv4)
	        {
	        	
	        //	System.out.println("I am a ICMP packet  ");
	        	return this.ctrlLogicWithRules(sw, (OFPacketIn) msg);
	      
	        }
	        

	       return Command.CONTINUE;
	        
	        

		
	  
	     
	    }
	    
}
