#include <iostream>
#include <netinet/in.h>
#include <time.h>
#include <string>

#define NUM_SUPPORTED_TYPES     8

#define A_RECORD                1
#define NS_RECORD               2
#define CNAME_RECORD            5
#define SOA_RECORD              6
#define PTR_RECORD              12
#define MX_RECORD               15
#define TXT_RECORD              16
#define AAAA_RECORD             28

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

void printResponses(uint16_t count);

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

char buf[0xffff];
time_t tmpTime;
uint16_t bodyLength, curPos, answerCount, authorityCount, additionalCount;
uint16_t rdataLength;
uint8_t nameLength;
uint16_t responseType;
uint64_t curFlow = 0;
uint32_t tmp;
string name;

int main() {
    while (1) {
        cin.read(buf, 32);
	if (cin.bad()) {
	    cerr << "Error reading header from input stream. Exiting..." << endl;
	    exit(1);
	}
	if (cin.gcount() != 32) {
	    cerr << "Reached EOF. No more flows. Goodbye..." << endl;
	    exit(0);
	}

	cout << "Header:" << endl;
	cout << "Template ID: " << (int) *(uint8_t *) (buf + TEMPLATE_ID) << endl;
	cout << "Body Length: " << (bodyLength = ntohs(*(uint16_t *) (buf + BODY_LENGTH))) << endl;
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
	
	cin.read(buf, bodyLength);
	if (cin.bad()) {
	    cerr << "Error reading body from input stream. Exiting..." << endl;
	    exit(1);
	}
	if (cin.gcount() != bodyLength) {
	    cerr << "DNS Flow body incomplete. Probably no more flows. Goodbye..." << endl;
	    exit(0);
	}

	cout << "DNS:" << endl;
	curPos = 0;
	cout << "Query Flags: " << ntohs(*(uint16_t *) (buf)) << endl;
	curPos += 2;
	cout << "Response Flags: " << ntohs(*(uint16_t *) (buf + curPos)) << endl;
	curPos += 2;
	nameLength = *(uint8_t *) (buf + curPos);
	cout << "Name Length: " << (int) nameLength << endl;
	++curPos;
	name.assign(buf + curPos, nameLength);
	cout << "Name: " << name << endl;
	curPos += nameLength;
	responseType = ntohs(*(uint16_t *) (buf + curPos));
	cout << "Query Type: ";
	switch (responseType) {
	  case A_RECORD:
	    cout << 'A';
	    break;
	  case NS_RECORD:
	    cout << "NS";
	    break;
	  case CNAME_RECORD:
	    cout << "CNAME";
	    break;
	  case SOA_RECORD:
	    cout << "SOA";
	    break;
	  case PTR_RECORD:
	    cout << "PTR";
	    break;
	  case MX_RECORD:
	    cout << "MX";
	    break;
	  case TXT_RECORD:
	    cout << "TXT";
	    break;
	  case AAAA_RECORD:
	    cout << "AAAA";
	    break;
	  default:
	    cout << "UNKNOWN: " << responseType;
	}
	cout << endl;
	curPos += 2;
	answerCount = ntohs(*(uint16_t *) (buf + curPos));
	cout << "Answer Count: " << answerCount << endl;
	curPos += 2;
	authorityCount = ntohs(*(uint16_t *) (buf + curPos));
	cout << "Authority Count: " << authorityCount << endl;
	curPos += 2;
	additionalCount = ntohs(*(uint16_t *) (buf + curPos));
	cout << "Additional Count: " << additionalCount << endl;
	curPos += 2;

	cout << "Answers:" << endl;
	printResponses(answerCount);

	cout << "Authorities:" << endl;
	printResponses(authorityCount);

	cout << "Additionals:" << endl;
	printResponses(additionalCount);
	
	++curFlow;

	cout << endl 
	     << "--------------------------------------------------------------------------------" << endl 
	     << endl;
	
	if (curPos != bodyLength) {
	    cerr << "ERROR: dns flow inconsistent...Exiting!" << endl;
	    cerr << "Last flow read was flow #" << curFlow << endl;
	    exit(1);
	}
    }

    return 0;
}

void printBytes(char* input, size_t size) {
  for (size_t index = 0; index < size; ++index) {
    std::cout << (int)(uint8_t)*(input + index) << ' ';
  }
  std::cout << std::endl;
}

void printResponses(uint16_t count) {
    for (uint16_t i = 0; i < count; ++i) {
	nameLength = *(uint8_t *) (buf + curPos);
	cout << i << "\tName Length: " << (int) nameLength << endl;
	++curPos;
	name.assign(buf + curPos, nameLength);
	cout << i << "\tName: " << name << endl;
	curPos += nameLength;
	responseType = ntohs(*(uint16_t *) (buf + curPos));
	cout << i << "\tResponse Type: ";
	switch (responseType) {
	  case A_RECORD:
	    cout << 'A';
	    break;
	  case NS_RECORD:
	    cout << "NS";
	    break;
	  case CNAME_RECORD:
	    cout << "CNAME";
	    break;
	  case SOA_RECORD:
	    cout << "SOA";
	    break;
	  case PTR_RECORD:
	    cout << "PTR";
	    break;
	  case MX_RECORD:
	    cout << "MX";
	    break;
	  case TXT_RECORD:
	    cout << "TXT";
	    break;
	  case AAAA_RECORD:
	    cout << "AAAA";
	    break;
	  default:
	    cout << "UNKNOWN: " << responseType;
	}
	cout << endl;
	curPos += 2;
	rdataLength = ntohs(*(uint16_t *) (buf + curPos));
	cout << i << "\tResource Data Length: " << rdataLength << endl;
	curPos += 2;
	cout << i << "\tResource Data: ";
	if (responseType == 1) {
	    printIP(ntohl(*(uint32_t *) (buf + curPos)));
	    cout << endl;
	} else {
	    name.assign(buf + curPos, rdataLength);
	    cout << name << endl;
	}
	curPos += rdataLength;
	tmp = ntohl(*(uint32_t *) (buf + curPos));
	cout << i << "\tTTL: " << *(int32_t *) &tmp << endl;
	curPos += 4;
    }
}
