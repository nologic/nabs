#ifndef INCLUDED_HASH_FUNCTION
#define INCLUDED_HASH_FUNCTION

/* SuperFastHash function (See benchmarks at http://www.azillionmonkeys.com/qed/hash.html) */
#undef get16bits
#if (defined(__GNUC__) && defined(__i386__)) || defined(__WATCOMC__) \
  || defined(_MSC_VER) || defined (__BORLANDC__) || defined (__TURBOC__)
#define get16bits(d) (*((const uint16_t *) (d)))
#endif

#if !defined (get16bits)
#define get16bits(d) ((((uint32_t)(((const uint8_t *)(d))[1])) << 8)\
                       +(uint32_t)(((const uint8_t *)(d))[0]) )
#endif

static inline uint32_t hash (const unsigned char * data, unsigned int len) {
uint32_t h = len, tmp;
int rem;

    if (len <= 0 || data == NULL) return 0;

    rem = len & 3;
    len >>= 2;

    /* Main loop */
    for (;len > 0; len--) {
        h  += get16bits (data);
        tmp    = (get16bits (data+2) << 11) ^ h;
        h   = (h << 16) ^ tmp;
        data  += 2*sizeof (uint16_t);
        h  += h >> 11;
    }

    /* Handle end cases */
    switch (rem) {
        case 3: h += get16bits (data);
                h ^= h << 16;
                h ^= data[sizeof (uint16_t)] << 18;
                h += h >> 11;
                break;
        case 2: h += get16bits (data);
                h ^= h << 11;
                h += h >> 17;
                break;
        case 1: h += *data;
                h ^= h << 10;
                h += h >> 1;
    }

    /* Force "avalanching" of final 127 bits */
    h ^= h << 3;
    h += h >> 5;
    h ^= h << 4;
    h += h >> 17;
    h ^= h << 25;
    h += h >> 6;

    return h;
}
#endif
