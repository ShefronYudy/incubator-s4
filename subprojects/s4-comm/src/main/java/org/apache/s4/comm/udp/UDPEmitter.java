package org.apache.s4.comm.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.s4.base.Emitter;
import org.apache.s4.comm.topology.ClusterNode;
import org.apache.s4.comm.topology.Topology;
import org.apache.s4.comm.topology.TopologyChangeListener;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBiMap;
import com.google.inject.Inject;

public class UDPEmitter implements Emitter, TopologyChangeListener {
    private DatagramSocket socket;
    private final HashBiMap<Integer, ClusterNode> nodes;
    private final Map<Integer, InetAddress> inetCache = new HashMap<Integer, InetAddress>();
    private final long messageDropInQueueCount = 0;
    private final Topology topology;

    public long getMessageDropInQueueCount() {
        return messageDropInQueueCount;
    }

    @Inject
    public UDPEmitter(Topology topology) {
        LoggerFactory.getLogger(getClass()).debug("UDPEmitter with topology {}",
                topology.getTopology().getPartitionCount());
        this.topology = topology;
        topology.addListener(this);
        nodes = HashBiMap.create(topology.getTopology().getNodes().size());
        for (ClusterNode node : topology.getTopology().getNodes()) {
            nodes.forcePut(node.getPartition(), node);
        }

        try {
            socket = new DatagramSocket();
        } catch (SocketException se) {
            throw new RuntimeException(se);
        }
    }

    @Override
    public boolean send(int partitionId, byte[] message) {
        try {
            ClusterNode node = nodes.get(partitionId);
            if (node == null) {
                LoggerFactory.getLogger(getClass()).error(
                        "Cannot send message to partition {} because this partition is not visible to this emitter",
                        partitionId);
                return false;
            }
            byte[] byteBuffer = new byte[message.length];
            System.arraycopy(message, 0, byteBuffer, 0, message.length);
            InetAddress inetAddress = inetCache.get(partitionId);
            if (inetAddress == null) {
                inetAddress = InetAddress.getByName(node.getMachineName());
                inetCache.put(partitionId, inetAddress);
            }
            DatagramPacket dp = new DatagramPacket(byteBuffer, byteBuffer.length, inetAddress, node.getPort());
            socket.send(dp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public int getPartitionCount() {
        return topology.getTopology().getPartitionCount();
    }

    @Override
    public void onChange() {
        // topology changes when processes pick tasks
        synchronized (nodes) {
            for (ClusterNode clusterNode : topology.getTopology().getNodes()) {
                Integer partition = clusterNode.getPartition();
                nodes.forcePut(partition, clusterNode);
            }
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }
}
