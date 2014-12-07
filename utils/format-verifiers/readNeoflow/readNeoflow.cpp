#include <iostream>
#include <netinet/in.h>
#include <time.h>

using namespace std;

const size_t TEMPLATE_ID = 0;
const size_t BODY_LENGTH = 1;
const size_t PROTOCOL = 3;
const size_t SOURCE_PORT = 4;
const size_t DEST_PORT = 6;
const size_t SOURCE_IP = 8;
const size_t DEST_IP = 12;
const size_t START_SECS = 16;
const size_t START_MSECS = 20;
const size_t END_SECS = 24;
const size_t END_MSECS = 28;

const size_t PACKET_COUNT = 32;
const size_t BYTE_COUNT = 36;
const size_t MIN_SIZE = 40;
const size_t MAX_SIZE = 44;
const size_t HISTO_0 = 48;
const size_t HISTO_1 = 52;
const size_t HISTO_2 = 56;
const size_t HISTO_3 = 60;
const size_t HISTO_4 = 64;
const size_t HISTO_5 = 68;
const size_t HISTO_6 = 72;
const size_t HISTO_7 = 76;
const size_t MIN_INTER_SECS = 80;
const size_t MIN_INTER_MSECS = 84;
const size_t MAX_INTER_SECS = 88;
const size_t MAX_INTER_MSECS = 92;
const size_t TOS = 96;
const size_t FRAGMENT_COUNT = 97;
const size_t MIN_TTL = 101;
const size_t MAX_TTL = 102;
const size_t URG_COUNT = 103;
const size_t ACK_COUNT = 107;
const size_t PUSH_COUNT = 111;
const size_t RST_COUNT = 115;
const size_t SYN_COUNT = 119;
const size_t FIN_COUNT = 123;
const size_t FIRST_SYN_SECS = 127;
const size_t FIRST_SYN_MSECS = 131;
const size_t FIRST_SYNACK_SECS = 135;
const size_t FIRST_SYNACK_MSECS = 139;
const size_t FIRST_ACK_SECS = 143;
const size_t FIRST_ACK_MSECS = 147;
const size_t CONTENT_0 = 151;
const size_t CONTENT_1 = 155;
const size_t CONTENT_2 = 159;
const size_t CONTENT_3 = 163;
const size_t CONTENT_4 = 167;
const size_t CONTENT_5 = 171;
const size_t CONTENT_6 = 175;
const size_t CONTENT_7 = 179;

void printIP(uint32_t ip) {
    uint8_t *p = ((uint8_t *) &ip) + 3;
    cout << (int) *p << '.';
    --p;
    cout << (int) *p << '.';
    --p;
    cout << (int) *p << '.';
    --p;
    cout << (int) *p;
}

int main() {
    char buf[183];
    time_t tmpTime;

    while (1) {
        cin.read(buf, 183);
	if (cin.bad()) {
	    cerr << "Error reading from input stream. Exiting..." << endl;
	    exit(1);
	}
	if (cin.gcount() != 183) {
	    cerr << "Reached EOF. No more flows. Goodbye..." << endl;
	    exit(0);
	}

	cout << "Header:" << endl;
	cout << "Template ID: " << (int) *(uint8_t *) (buf + TEMPLATE_ID) << endl;
	cout << "Body Length: " << ntohs(*(uint16_t *) (buf + BODY_LENGTH)) << endl;
	cout << "Protocol: " << (int) *(uint8_t *) (buf + PROTOCOL) << endl;
	cout << "Source Port: " << ntohs(*(uint16_t *) (buf + SOURCE_PORT)) << endl;
	cout << "Destination Port: " << ntohs(*(uint16_t *) (buf + DEST_PORT)) << endl;
	cout << "Source IP: "; printIP(ntohl(*(uint32_t *) (buf + SOURCE_IP))); cout << endl;
	cout << "Destination IP: "; printIP(ntohl(*(uint32_t *) (buf + DEST_IP))); cout << endl;
	tmpTime = ntohl(*(uint32_t *) (buf + START_SECS));
	cout << "Start Time Seconds: " << ctime(&tmpTime); // << endl;
	cout << "Start Time Microseconds: " << ntohl(*(uint32_t *) (buf + START_MSECS)) << endl;
 	tmpTime = ntohl(*(uint32_t *) (buf + END_SECS));
	cout << "End Time Seconds: " << ctime(&tmpTime); // << endl;
	cout << "End Time Microseconds: " << ntohl(*(uint32_t *) (buf + END_MSECS)) << endl << endl;
	
	cout << "Neoflow:" << endl;
	cout << "Packet Count: " << ntohl(*(uint32_t *) (buf + PACKET_COUNT)) << endl;
	cout << "Byte Count: " << ntohl(*(uint32_t *) (buf + BYTE_COUNT)) << endl;
	cout << "Min Packet Size: " << ntohl(*(uint32_t *) (buf + MIN_SIZE)) << endl;
	cout << "Max Packet Size: " << ntohl(*(uint32_t *) (buf + MAX_SIZE)) << endl;
	cout << "Histogram [0, 256]: " << ntohl(*(uint32_t *) (buf + HISTO_0)) << endl;
	cout << "Histogram [257, 512]: " << ntohl(*(uint32_t *) (buf + HISTO_1)) << endl;
	cout << "Histogram [513, 768]: " << ntohl(*(uint32_t *) (buf + HISTO_2)) << endl;
	cout << "Histogram [769, 1024]: " << ntohl(*(uint32_t *) (buf + HISTO_3)) << endl;
	cout << "Histogram [1025, 1280]: " << ntohl(*(uint32_t *) (buf + HISTO_4)) << endl;
	cout << "Histogram [1281, 1536]: " << ntohl(*(uint32_t *) (buf + HISTO_5)) << endl;
	cout << "Histogram [1537, 1792]: " << ntohl(*(uint32_t *) (buf + HISTO_6)) << endl;
	cout << "Histogram [1793, 2048]: " << ntohl(*(uint32_t *) (buf + HISTO_7)) << endl;
	cout << "Min Inter-arrival Seconds: " << ntohl(*(uint32_t *) (buf + MIN_INTER_SECS)) << endl;
	cout << "Min Inter-arrival Microseconds: " << ntohl(*(uint32_t *) (buf + MIN_INTER_MSECS)) << endl;
	cout << "Max Inter-arrival Seconds: " << ntohl(*(uint32_t *) (buf + MAX_INTER_SECS)) << endl;
	cout << "Max Inter-Arrival Microseconds: " << ntohl(*(uint32_t *) (buf + MAX_INTER_MSECS)) << endl;
	cout << "Type of Service: " << (int) *(uint8_t *) (buf + TOS) << endl;
	cout << "Fragment Count: " << ntohl(*(uint32_t *) (buf + FRAGMENT_COUNT)) << endl;
	cout << "Min TTL: " << (int) *(uint8_t *) (buf + MIN_TTL) << endl;
	cout << "Max TTL: " << (int) *(uint32_t *) (buf + MAX_TTL) << endl;
	cout << "URG Count: " << ntohl(*(uint32_t *) (buf + URG_COUNT)) << endl;
	cout << "ACK Count: " << ntohl(*(uint32_t *) (buf + ACK_COUNT)) << endl;
	cout << "PUSH Count: " << ntohl(*(uint32_t *) (buf + PUSH_COUNT)) << endl;
	cout << "RST Count: " << ntohl(*(uint32_t *) (buf + RST_COUNT)) << endl;
	cout << "SYN Count: " << ntohl(*(uint32_t *) (buf + SYN_COUNT)) << endl;
	cout << "FIN Count: " << ntohl(*(uint32_t *) (buf + FIN_COUNT)) << endl;
	cout << "First SYN Seconds: " << ntohl(*(uint32_t *) (buf + FIRST_SYN_SECS)) << endl;
	cout << "First SYN Microseconds: " << ntohl(*(uint32_t *) (buf + FIRST_SYN_MSECS)) << endl;
	cout << "First SYN-ACK Seconds: " << ntohl(*(uint32_t *) (buf + FIRST_SYNACK_SECS)) << endl;
	cout << "First SYN-ACK Microseconds: " << ntohl(*(uint32_t *) (buf + FIRST_SYNACK_MSECS)) << endl;
	cout << "First ACK Seconds: " << ntohl(*(uint32_t *) (buf + FIRST_ACK_SECS)) << endl;
	cout << "First ACK Microseconds: " << ntohl(*(uint32_t *) (buf + FIRST_ACK_MSECS)) << endl;
	cout << "Plaintext Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_0)) << endl;
	cout << "BMP Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_1)) << endl;
	cout << "WAV Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_2)) << endl;
	cout << "Compressed Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_3)) << endl;
	cout << "JPEG Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_4)) << endl;
	cout << "MP3 Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_5)) << endl;
	cout << "MPEG Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_6)) << endl;
	cout << "Encrypted Data Bytes: " << ntohl(*(uint32_t *) (buf + CONTENT_7)) << endl << endl;
	cout << "--------------------------------------------------------------------------------" << endl << endl;
    }

    return 0;
}

