package net.floodlightcontroller.MyNewApptest;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import java.util.Arrays;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.test.MockFloodlightProvider;
import net.floodlightcontroller.MyNewApp.MyNewApp;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.test.FloodlightTestCase;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Before;
import org.junit.Test;


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


public class MyNewApptestTest extends FloodlightTestCase{
    
     protected OFPacketIn packetIn_1;
     protected OFPacketIn packetIn_2;
        protected IPacket testPacket_1;
        protected IPacket testPacket_2;
        protected byte[] testPacket_1Serialized;
        protected byte[] testPacket_2Serialized;
        
        private   MockFloodlightProvider mockFloodlightProvider;
        private MyNewApp ipspoof;
        private boolean flag;
        
        @Before
        public void setUp() throws Exception {
            super.setUp();

            mockFloodlightProvider = getMockFloodlightProvider();
            ipspoof = new MyNewApp();
            mockFloodlightProvider.addOFMessageListener(OFType.PACKET_IN,ipspoof);
            ipspoof.setFloodlightProvider(mockFloodlightProvider);
            
            // Build our test packet
            this.testPacket_1 = new Ethernet()
                .setDestinationMACAddress("00:00:00:00:00:01")
                .setSourceMACAddress("00:00:00:00:00:02")
                .setEtherType(Ethernet.TYPE_IPv4)
                .setPayload(
                    new IPv4()
                    .setTtl((byte)64)
                    .setSourceAddress("10.0.2.15")
                    .setDestinationAddress("10.0.2.3")
                     .setIdentification((short)1233)
                    .setPayload(new UDP()
                                .setSourcePort((short) 5000)
                                .setDestinationPort((short) 5001)
                                .setPayload(new Data(new byte[] {0x01}))));
            
            this.testPacket_2 = new Ethernet()
            .setDestinationMACAddress("00:00:00:00:00:01")
            .setSourceMACAddress("00:00:00:00:00:02")
            .setEtherType(Ethernet.TYPE_IPv4)
            .setPayload(
                new IPv4()
                .setTtl((byte)64)
                .setSourceAddress("10.0.2.15")
                .setDestinationAddress("10.0.2.3")
                .setIdentification((short)1234)
                .setPayload(new UDP()
                            .setSourcePort((short) 5000)
                            .setDestinationPort((short) 5001)
                            .setPayload(new Data(new byte[] {0x01}))));
            
            
            this.testPacket_1Serialized = testPacket_1.serialize();
            
            this.testPacket_2Serialized = testPacket_2.serialize();

            

            // Build the PacketIn
            this.packetIn_1 = ((OFPacketIn) mockFloodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_IN))
                .setPacketData(this.testPacket_1Serialized)
                .setTotalLength((short) this.testPacket_1Serialized.length);
            
            this.packetIn_2 = ((OFPacketIn) mockFloodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_IN))
                    .setPacketData(this.testPacket_2Serialized)
                    .setTotalLength((short) this.testPacket_2Serialized.length);
        }

        @Test
        public void testFloodNoBufferId() throws Exception {
            OFPacketOut po_1 = ((OFPacketOut) mockFloodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT))
                .setPacketData(this.testPacket_1Serialized);
            po_1.setLengthU(OFPacketOut.MINIMUM_LENGTH + po_1.getActionsLengthU()
                    + this.testPacket_1Serialized.length);
            
            OFPacketOut po_2 = ((OFPacketOut) mockFloodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT))
                    .setPacketData(this.testPacket_2Serialized);
                po_2.setLengthU(OFPacketOut.MINIMUM_LENGTH + po_2.getActionsLengthU()
                        + this.testPacket_2Serialized.length);

            IOFSwitch testSwitch = createMock(IOFSwitch.class);

            IOFMessageListener listener = mockFloodlightProvider.getListeners().get(OFType.PACKET_IN).get(0);
            listener.receive(testSwitch, this.packetIn_1,
                             parseAndAnnotate(this.packetIn_1));
            

             listener = mockFloodlightProvider.getListeners().get(OFType.PACKET_IN).get(0);
            listener.receive(testSwitch, this.packetIn_2,
                             parseAndAnnotate(this.packetIn_2));
            
            flag = ipspoof.isspoofed();
            assertEquals(false, flag);
            
           
        }

}
