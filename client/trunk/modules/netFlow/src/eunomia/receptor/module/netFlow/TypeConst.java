/*
 * TypeConst.java
 *
 * Created on August 28, 2006, 11:03 PM
 *
 */

package eunomia.receptor.module.netFlow;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class TypeConst {
    public static final int TYPE_COUNT = 89;
    public static final String[] TYPE_LABEL = new String[TYPE_COUNT + 1];

    //                                                  Length          Description
    public static final short IN_BYTES            = 1;    // N (default is 4) Incoming counter with length N x 8 bits for number of bytes associated with an IP Flow.
    public static final short IN_PKTS             = 2;    // N (default is 4) Incoming counter with length N x 8 bits for the number of packets associated with an IP Flow
    public static final short FLOWS               = 3;    // N                Number of flows that were aggregated; default for N is 4
    public static final short PROTOCOL            = 4;    // 1                IP protocol byte
    public static final short SRC_TOS             = 5;    // 1                Type of Service byte setting when entering incoming interface
    public static final short TCP_FLAGS           = 6;    // 1                Cumulative of all the TCP flags seen for this flow
    public static final short L4_SRC_PORT         = 7;    // 2                TCP/UDP source port number e.g. FTP, Telnet, or equivalent
    public static final short IPV4_SRC_ADDR       = 8;    // 4                IPv4 source address
    public static final short SRC_MASK            = 9;    // 1                The number of contiguous bits in the source address subnet mask i.e. the submask in slash notation
    public static final short INPUT_SNMP          = 10;   // N                Input interface index; default for N is 2 but higher values could be used
    public static final short L4_DST_PORT         = 11;   // 2                TCP/UDP destination port number e.g. FTP, Telnet, or equivalent
    public static final short IPV4_DST_ADDR       = 12;   // 4                IPv4 destination address
    public static final short DST_MASK            = 13;   // 1                The number of contiguous bits in the destination address subnet mask i.e. the submask in slash notation
    public static final short OUTPUT_SNMP         = 14;   // N                Output interface index; default for N is 2 but higher values could be used
    public static final short IPV4_NEXT_HOP       = 15;   // 4                IPv4 address of next-hop router
    public static final short SRC_AS              = 16;   // N (default is 2) Source BGP autonomous system number where N could be 2 or 4
    public static final short DST_AS              = 17;   // N (default is 2) Destination BGP autonomous system number where N could be 2 or 4
    public static final short BGP_IPV4_NEXT_HOP   = 18;   // 4                Next-hop router's IP in the BGP domain
    public static final short MUL_DST_PKTS        = 19;   // N (default is 4) IP multicast outgoing packet counter with length N x 8 bits for packets associated with the IP Flow
    public static final short MUL_DST_BYTES       = 20;   // N (default is 4) IP multicast outgoing byte counter with length N x 8 bits for bytes associated with the IP Flow
    public static final short LAST_SWITCHED       = 21;   // 4                System uptime at which the last packet of this flow was switched
    public static final short FIRST_SWITCHED      = 22;   // 4                System uptime at which the first packet of this flow was switched
    public static final short OUT_BYTES           = 23;   // N (default is 4) Outgoing counter with length N x 8 bits for the number of bytes associated with an IP Flow
    public static final short OUT_PKTS            = 24;   // N (default is 4) Outgoing counter with length N x 8 bits for the number of packets associated with an IP Flow.
    public static final short MIN_PKT_LNGTH       = 25;   // 2                Minimum IP packet length on incoming packets of the flow
    public static final short MAX_PKT_LNGTH       = 26;   // 2                Maximum IP packet length on incoming packets of the flow
    public static final short IPV6_SRC_ADDR       = 27;   // 16               IPv6 Source Address
    public static final short IPV6_DST_ADDR       = 28;   // 16               IPv6 Destination Address
    public static final short IPV6_SRC_MASK       = 29;   // 1                Length of the IPv6 source mask in contiguous bits
    public static final short IPV6_DST_MASK       = 30;   // 1                Length of the IPv6 destination mask in contiguous bits
    public static final short IPV6_FLOW_LABEL     = 31;   // 3                IPv6 flow label as per RFC 2460definition
    public static final short ICMP_TYPE           = 32;   // 2                Internet Control Message Protocol (ICMP) packet type; reported as ((ICMP Type * 256) + ICMP code)
    public static final short MUL_IGMP_TYPE       = 33;   // 1                Internet Group Management Protocol (IGMP) packet type
    public static final short SAMPLING_INTERVAL   = 34;   // 4                When using sampled NetFlow, the rate at which packets are sampled e.g. a value of 100 indicates that one of every 100 packets is sampled
    public static final short SAMPLING_ALGORITHM  = 35;   // 1                The type of algorithm used for sampled NetFlow: 0x01 Deterministic Sampling, 0x02 Random Sampling
    public static final short FLOW_ACTIVE_TIMEOUT = 36;   // 2                Timeout value (in seconds) for active flow entries in the NetFlow cache
    public static final short FLOW_INACTIVE_TIMEOUT = 37; // 2                Timeout value (in seconds) for inactive flow entries in the NetFlow cache
    public static final short ENGINE_TYPE         = 38;   // 1                Type of flow switching engine: RP = 0, VIP/Linecard = 1
    public static final short ENGINE_ID           = 39;   // 1                ID number of the flow switching engine
    public static final short TOTAL_BYTES_EXP     = 40;   // N (default is 4) Counter with length N x 8 bits for bytes for the number of bytes exported by the Observation Domain
    public static final short TOTAL_PKTS_EXP      = 41;   // N (default is 4) Counter with length N x 8 bits for bytes for the number of packets exported by the Observation Domain
    public static final short TOTAL_FLOWS_EXP     = 42;   // N (default is 4) Counter with length N x 8 bits for bytes for the number of flows exported by the Observation Domain
    // Vendor Proprietary* 43
    public static final short IPV4_SRC_PREFIX     = 44;   // 4                IPv4 source address prefix (specific for Catalyst architecture)
    public static final short IPV4_DST_PREFIX     = 45;   // 4                IPv4 destination address prefix (specific for Catalyst architecture)
    public static final short MPLS_TOP_LABEL_TYPE = 46;   // 1                MPLS Top Label Type: 0x00 UNKNOWN 0x01 TE-MIDPT 0x02 ATOM 0x03 VPN 0x04 BGP 0x05 LDP
    public static final short MPLS_TOP_LABEL_IP_ADDR = 47; // 4               Forwarding Equivalent Class corresponding to the MPLS Top Label
    public static final short FLOW_SAMPLER_ID     = 48;   // 1                Identifier shown in "show flow-sampler"
    public static final short FLOW_SAMPLER_MODE   = 49;   // 1                The type of algorithm used for sampling data: 0x02 random sampling. Use in connection with FLOW_SAMPLER_MODE 
    public static final short FLOW_SAMPLER_RANDOM_INTERVAL = 50; //4          Packet interval at which to sample. Use in connection with FLOW_SAMPLER_MODE
    // Vendor Proprietary* 51
    public static final short MIN_TTL             = 52;   // 1                Minimum TTL on incoming packets of the flow
    public static final short MAX_TTL             = 53;   // 1                Maximum TTL on incoming packets of the flow
    public static final short IPV4_IDENT          = 54;   // 2                The IP v4 identification field
    public static final short DST_TOS             = 55;   // 1                Type of Service byte setting when exiting outgoing interface
    public static final short IN_SRC_MAC          = 56;   // 6                Incoming source MAC address
    public static final short OUT_DST_MAC         = 57;   // 6                Outgoing destination MAC address
    public static final short SRC_VLAN            = 58;   // 2                Virtual LAN identifier associated with ingress interface
    public static final short DST_VLAN            = 59;   // 2                Virtual LAN identifier associated with egress interface
    public static final short IP_PROTOCOL_VERSION = 60;   // 1                Internet Protocol Version Set to 4 for IPv4, set to 6 for IPv6. If not presentin the template, then version 4 is assumed.
    public static final short DIRECTION           = 61;   // 1                Flow direction: 0 - ingress flow, 1 - egress flow
    public static final short IPV6_NEXT_HOP       = 62;   // 16               IPv6 address of the next-hop router
    public static final short BPG_IPV6_NEXT_HOP   = 63;   // 16               Next-hop router in the BGP domain
    public static final short IPV6_OPTION_HEADERS = 64;   // 4                Bit-encoded field identifying IPv6 option headers found in the flow
    // Vendor Proprietary* 65
    // Vendor Proprietary* 66
    // Vendor Proprietary* 67
    // Vendor Proprietary* 68
    // Vendor Proprietary* 69
    public static final short MPLS_LABEL_1        = 70;   // 3                MPLS label at position 1 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_2        = 71;   // 3                MPLS label at position 2 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_3        = 72;   // 3                MPLS label at position 3 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_4        = 73;   // 3                MPLS label at position 4 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_5        = 74;   // 3                MPLS label at position 5 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_6        = 75;   // 3                MPLS label at position 6 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_7        = 76;   // 3                MPLS label at position 7 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_8        = 77;   // 3                MPLS label at position 8 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_9        = 78;   // 3                MPLS label at position 9 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short MPLS_LABEL_10       = 79;   // 3                MPLS label at position 10 in the stack. This comprises 20 bits of MPLS label, 3 EXP (experimental) bits and 1 S (end-of-stack) bit.
    public static final short IN_DST_MAC          = 80;   // 6                Incoming destination MAC address
    public static final short OUT_SRC_MAC         = 81;   // 6                Outgoing source MAC address
    public static final short IF_NAME             = 82;   // N (default specified in template) Shortened interface name e.g. "FE1/0"
    public static final short IF_DESC             = 83;   // N (default specified in template) Full interface name e.g. "'FastEthernet 1/0"
    public static final short SAMPLER_NAME        = 84;   // N (default specified in template) Name of the flow sampler
    public static final short IN_PERMANENT_BYTES  = 85;   // N (default is 4) Running byte counter for a permanent flow
    public static final short IN_PERMANENT_PKTS   = 86;   // N (default is 4) Running packet counter for a permanent flow
    // Vendor Proprietary* 87
    public static final short FRAGMENT_OFFSET     = 88;   // 2                The fragment-offset value from fragmented IP packets
    public static final short FORWARDING_STATUS   = 89;   // 1                Forwarding status with values: Unknown 0, Normal forwarding 1, Forward fragmented 2, Drop 16, Drop ACL Deny 17, Drop ACL drop 18, Drop Unroutable 19, Drop Adjacency 20, Drop Fragmentation & DF set 21, Drop Bad header checksum 22, Drop Bad total Length 23, Drop Bad Header Length 24, Drop bad TTL 25, Drop Policer 26, Drop WRED 27, Drop RPF 28, Drop For us 29, Drop Bad output interface 30, Drop Hardware 31, Terminate 128, Terminate Punt Adjacency 129, Terminate Incomplete Adjacency 130 and Terminate For us 131

    static {
        TYPE_LABEL[IN_BYTES] = "IN_BYTES";
        TYPE_LABEL[IN_PKTS] = "IN_PKTS";
        TYPE_LABEL[FLOWS] = "FLOWS";
        TYPE_LABEL[PROTOCOL] = "PROTOCOL";
        TYPE_LABEL[SRC_TOS] = "SRC_TOS";
        TYPE_LABEL[TCP_FLAGS] = "TCP_FLAGS";
        TYPE_LABEL[L4_SRC_PORT] = "L4_SRC_PORT";
        TYPE_LABEL[IPV4_SRC_ADDR] = "IPV4_SRC_ADDR";
        TYPE_LABEL[SRC_MASK] = "SRC_MASK";
        TYPE_LABEL[INPUT_SNMP] = "INPUT_SNMP";
        TYPE_LABEL[L4_DST_PORT] = "L4_DST_PORT";
        TYPE_LABEL[IPV4_DST_ADDR] = "IPV4_DST_ADDR";
        TYPE_LABEL[DST_MASK] = "DST_MASK";
        TYPE_LABEL[OUTPUT_SNMP] = "OUTPUT_SNMP";
        TYPE_LABEL[IPV4_NEXT_HOP] = "IPV4_NEXT_HOP";
        TYPE_LABEL[SRC_AS] = "SRC_AS";
        TYPE_LABEL[DST_AS] = "DST_AS";
        TYPE_LABEL[BGP_IPV4_NEXT_HOP] = "BGP_IPV4_NEXT_HOP";
        TYPE_LABEL[MUL_DST_PKTS] = "MUL_DST_PKTS";
        TYPE_LABEL[MUL_DST_BYTES] = "MUL_DST_BYTES";
        TYPE_LABEL[LAST_SWITCHED] = "LAST_SWITCHED";
        TYPE_LABEL[FIRST_SWITCHED] = "FIRST_SWITCHED";
        TYPE_LABEL[OUT_BYTES] = "OUT_BYTES";
        TYPE_LABEL[OUT_PKTS] = "OUT_PKTS";
        TYPE_LABEL[MIN_PKT_LNGTH] = "MIN_PKT_LNGTH";
        TYPE_LABEL[MAX_PKT_LNGTH] = "MAX_PKT_LNGTH";
        TYPE_LABEL[IPV6_SRC_ADDR] = "IPV6_SRC_ADDR";
        TYPE_LABEL[IPV6_DST_ADDR] = "IPV6_DST_ADDR";
        TYPE_LABEL[IPV6_SRC_MASK] = "IPV6_SRC_MASK";
        TYPE_LABEL[IPV6_DST_MASK] = "IPV6_DST_MASK";
        TYPE_LABEL[IPV6_FLOW_LABEL] = "IPV6_FLOW_LABEL";
        TYPE_LABEL[ICMP_TYPE] = "ICMP_TYPE";
        TYPE_LABEL[MUL_IGMP_TYPE] = "MUL_IGMP_TYPE";
        TYPE_LABEL[SAMPLING_INTERVAL] = "SAMPLING_INTERVAL";
        TYPE_LABEL[SAMPLING_ALGORITHM] = "SAMPLING_ALGORITHM";
        TYPE_LABEL[FLOW_ACTIVE_TIMEOUT] = "FLOW_ACTIVE_TIMEOUT";
        TYPE_LABEL[FLOW_INACTIVE_TIMEOUT] = "FLOW_INACTIVE_TIMEOUT";
        TYPE_LABEL[ENGINE_TYPE] = "ENGINE_TYPE";
        TYPE_LABEL[ENGINE_ID] = "ENGINE_ID";
        TYPE_LABEL[TOTAL_BYTES_EXP] = "TOTAL_BYTES_EXP";
        TYPE_LABEL[TOTAL_PKTS_EXP] = "TOTAL_PKTS_EXP";
        TYPE_LABEL[TOTAL_FLOWS_EXP] = "TOTAL_FLOWS_EXP";
        TYPE_LABEL[IPV4_SRC_PREFIX] = "IPV4_SRC_PREFIX";
        TYPE_LABEL[IPV4_DST_PREFIX] = "IPV4_DST_PREFIX";
        TYPE_LABEL[MPLS_TOP_LABEL_TYPE] = "MPLS_TOP_LABEL_TYPE";
        TYPE_LABEL[MPLS_TOP_LABEL_IP_ADDR] = "MPLS_TOP_LABEL_IP_ADDR";
        TYPE_LABEL[FLOW_SAMPLER_ID] = "FLOW_SAMPLER_ID";
        TYPE_LABEL[FLOW_SAMPLER_MODE] = "FLOW_SAMPLER_MODE";
        TYPE_LABEL[FLOW_SAMPLER_RANDOM_INTERVAL] = "FLOW_SAMPLER_RANDOM_INTERVAL";
        TYPE_LABEL[MIN_TTL] = "MIN_TTL";
        TYPE_LABEL[MAX_TTL] = "MAX_TTL";
        TYPE_LABEL[IPV4_IDENT] = "IPV4_IDENT";
        TYPE_LABEL[DST_TOS] = "DST_TOS";
        TYPE_LABEL[IN_SRC_MAC] = "IN_SRC_MAC";
        TYPE_LABEL[OUT_DST_MAC] = "OUT_DST_MAC";
        TYPE_LABEL[SRC_VLAN] = "SRC_VLAN";
        TYPE_LABEL[DST_VLAN] = "DST_VLAN";
        TYPE_LABEL[IP_PROTOCOL_VERSION] = "IP_PROTOCOL_VERSION";
        TYPE_LABEL[DIRECTION] = "DIRECTION";
        TYPE_LABEL[IPV6_NEXT_HOP] = "IPV6_NEXT_HOP";
        TYPE_LABEL[BPG_IPV6_NEXT_HOP] = "BPG_IPV6_NEXT_HOP";
        TYPE_LABEL[IPV6_OPTION_HEADERS] = "IPV6_OPTION_HEADERS";
        TYPE_LABEL[MPLS_LABEL_1] = "MPLS_LABEL_1";
        TYPE_LABEL[MPLS_LABEL_2] = "MPLS_LABEL_2";
        TYPE_LABEL[MPLS_LABEL_3] = "MPLS_LABEL_3";
        TYPE_LABEL[MPLS_LABEL_4] = "MPLS_LABEL_4";
        TYPE_LABEL[MPLS_LABEL_5] = "MPLS_LABEL_5";
        TYPE_LABEL[MPLS_LABEL_6] = "MPLS_LABEL_6";
        TYPE_LABEL[MPLS_LABEL_7] = "MPLS_LABEL_7";
        TYPE_LABEL[MPLS_LABEL_8] = "MPLS_LABEL_8";
        TYPE_LABEL[MPLS_LABEL_9] = "MPLS_LABEL_9";
        TYPE_LABEL[MPLS_LABEL_10] = "MPLS_LABEL_10";
        TYPE_LABEL[IN_DST_MAC] = "IN_DST_MAC";
        TYPE_LABEL[OUT_SRC_MAC] = "OUT_SRC_MAC";
        TYPE_LABEL[IF_NAME] = "IF_NAME";
        TYPE_LABEL[IF_DESC] = "IF_DESC";
        TYPE_LABEL[SAMPLER_NAME] = "SAMPLER_NAME";
        TYPE_LABEL[IN_PERMANENT_BYTES] = "IN_PERMANENT_BYTES";
        TYPE_LABEL[IN_PERMANENT_PKTS] = "IN_PERMANENT_PKTS";
        TYPE_LABEL[FRAGMENT_OFFSET] = "FRAGMENT_OFFSET";
        TYPE_LABEL[FORWARDING_STATUS] = "FORWARDING_STATUS";
    }
}