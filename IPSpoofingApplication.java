package net.floodlightcontroller.MyNewApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.io.InputStreamReader;

import net.floodlightcontroller.core.IFloodlightProviderService;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

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
import net.floodlightcontroller.packet.BasePacket;

import java.util.Arrays;

import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.RoutingDecision;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

import java.util.regex.Pattern;

import net.floodlightcontroller.pktinhistory.ConcurrentCircularBuffer;
import net.floodlightcontroller.core.types.SwitchMessagePair;
import net.floodlightcontroller.core.IFloodlightProviderService;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

import net.floodlightcontroller.packet.Ethernet;

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
import net.floodlightcontroller.packet.BasePacket;

import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;

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



public class MyNewApp implements IOFMessageListener, IFloodlightModule {
    protected IFloodlightProviderService floodlightProvider;
    protected ConcurrentCircularBuffer<SwitchMessagePair> buffer;
    protected byte hop1;
    protected byte possible1_hop2;
    protected byte possible2_hop2;
    private BasePacket pkt;
    private InetAddress inet;
    private String ping_cmd;
    private String inputLine;
    private BufferedReader in;
    private java.util.regex.Matcher check_ttl;
    private java.util.regex.Matcher check_seq;
    private Pattern pattern_ttl;
    private Pattern pattern_seq;
    private IPv4 ip;
    private byte ttl1;
    private byte ttl2;
    private int seq;
    private Ethernet eth;
    private byte protocol ;
    private int iterations ;
    private int no_of_pings ;
    protected boolean set;
    private Map<Integer, Long> hm = new HashMap<Integer, Long>();
    private int i;
    private int  threshold ;
    
    
    

    @Override
    public String getName() {
        return "MyNewApp";
    }
    public boolean isspoofed()
    {
        if (set == true )
            return true;
        else
            return false;
    }
    
    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }

    public BasePacket getPkt() {
        return pkt;
    }

    public void setPkt(BasePacket pkt) {
        this.pkt = pkt;
    }

    public InetAddress getInet() {
        return inet;
    }

    public void setInet(InetAddress inet) {
        this.inet = inet;
    }
    
    public void setFloodlightProvider(IFloodlightProviderService floodlightProvider) {
        this.floodlightProvider = floodlightProvider;
    }

    @Override
     public boolean isCallbackOrderingPrereq(OFType type, String name) {
      return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
    return false;
    }


    @Override
    public net.floodlightcontroller.core.IListener.Command receive(
            IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch(msg.getType()) {
        case PACKET_IN:
            setPkt((BasePacket) IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD));
               
             OFPacketIn pin = (OFPacketIn) msg;
             OFMatch match = new OFMatch();
                 
            eth = IFloodlightProviderService.bcStore.get(cntx,
                                        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
            
            
        //    System.out.println("i am receiving packets ");
            
            threshold = 100;
            
            if(eth.getEtherType() == Ethernet.TYPE_IPv4){
                //Extract the IP Packet from ethernet packet
                 ip = (IPv4)eth.getPayload();      
                  protocol = ip.getProtocol();
                 /*
                 System.out.println(+protocol);
                 */
                  
                 //Since we got UDP packet so need to subtract 128 or 64 from ttl1;
            }
            
           if(ip!= null)
           {
                //Find the ttl of IP Packet
                ttl1 = ip.getTtl();  
             //   System.out.println("Printing ttl value first time " +ttl1);
                long ident1 = ip.getIdentification();
                
                hm.put(i ,  ident1);
             
                
                
                
             
          //      System.out.println(hm.get(i));
                
                i= i+1;
                /*
                System.out.println("Here is the identification time ");
                System.out.print(i);
                System.out.println("Printing ident1 value  time "+ident1);
                */
                
              //Means it is a UDP packet or TCP packet
                if(protocol == 17 || protocol == 06)
                   	hop1 =  (byte) ((128) - ttl1);
               
              // Means it is a ICMP packet   
                if(protocol == 1)
                	hop1 =  (byte) ((255) - ttl1);
                
           } 
            
            match.loadFromPacket(pin.getPacketData(), pin.getInPort());
                      
            String sourceip = IPv4.fromIPv4Address(match.getNetworkSource());
         
               
			try {
				setInet(InetAddress.getByName(sourceip));
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	/*
           
            System.out.println("Sending Ping Request to " + sourceip);
            
            */
            
            try {
          
                if (inet.isReachable(1)) {
                    pattern_ttl = Pattern.compile("ttl=(\\d\\d)");
            
                    
                    check_ttl = null;
                    check_seq = null;
                    ping_cmd = "ping " + sourceip;
                    Runtime r = Runtime.getRuntime();
                    Process p = r.exec(ping_cmd);
                    
                  //  System.out.println("Did i come here");
               
                    in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    
                    // number of iterations required to cross check 
      
                    
                    while ((inputLine = in.readLine()) != null) {
                    //	System.out.println(inputLine);
                        check_ttl = pattern_ttl.matcher(inputLine);
                      
                        
                        /*
                        if(check_seq.find()) {
                        	seq =  Integer.parseInt(check_seq.group(1));
                        	System.out.println("Seq number of the packet is " +seq);
                        }
                        
                        */
                        
                       
                        if (check_ttl.find()) {
                            // Extract the ttl value
                            ttl2 = (byte) Integer.parseInt(check_ttl.group(1));
                      //     System.out.println("Printing ttl value second time " +ttl2);
                            
                            possible1_hop2 = (byte) (128 - ttl2);
                            possible2_hop2 = (byte)(255-ttl2);
                            
                            if (hop1 == possible1_hop2 || hop1 == possible2_hop2)  {
                            	if(set == false)
                            	{
                            		if(i==1)
                            			set = true;                         			
                            		else
                            			set = false;
                            		
                            	   break;
                                }
                            else
                            	{
                            		set = true;
                            		break;
                            	}
                            }
                            else
                            {
                            	set= false ;
                            	break;
                            }
                            
                     
                            
                            //From the above output cannot find 
                            
                           
                        }
                      	
                    }
                    
                    
                    if(i==2)
                    {
                    	Long ident_curr ;
                    	Long ident_prev;
                    	ident_curr = hm.get(i-1);
                    	ident_prev = hm.get(i-2);
                    	
                  //  	System.out.println(ident_curr);
                  //  	System.out.println(ident_prev);
                  //  	
                    	long diff = (ident_curr - ident_prev);
                    	
                    	diff = (diff < 0) ? -diff : diff;
                    //	System.out.println(diff);
                    	
                  //   	System.out.println(threshold);
                     	
                     	
                    	if(diff > threshold) {
                    	//	System.out.print("came at exceeded threshold value");
                    		set = false;
                    		
                    	}	
                    	else
                    	{
                    	
                    	//	System.out.print("came at below threshold value");
                    		if(set == false)
                    			set = false;
                    		else
                    		set = true;
                    	
                    	}	
                    	
                    }	
                    
                    
                    
                    in.close();
               
                  /*  
                    System.out.println("Possible hops ");
                    System.out.println(hop1);
                    System.out.println(possible1_hop2);
                    System.out.println(possible2_hop2);
                  */  
                    
                    
                    
              if(i ==2 )
              {
                    if (set == true) {
                        
                        System.out.println("NOT A SPOOFED PACKET ");
                    } else {
                        set= false;
                        System.out.println("SPOOFED PACKET");
                    }
              } 
                } else {
                    set= false;
                    System.out.println("SPOOFED PACKET ");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
                
            break;
       default:
           break;
    }
    return Command.CONTINUE;
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
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;

    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        buffer = new ConcurrentCircularBuffer<SwitchMessagePair>(SwitchMessagePair.class, 100);
        no_of_pings = 5;
        iterations =0;
        i=0;
        threshold =  100;

    }

  

}
