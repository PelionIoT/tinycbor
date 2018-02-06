//----------------------------------------------------------------------------
//   The confidential and proprietary information contained in this file may
//   only be used by a person authorised under and to the extent permitted
//   by a subsisting licensing agreement from ARM Limited or its affiliates.
//
//          (C) COPYRIGHT 2013-2016 ARM Limited or its affiliates.
//              ALL RIGHTS RESERVED
//
//   This entire notice must be reproduced on all copies of this file
//   and copies of this file may only be made by a person if such person is
//   permitted to do so under the terms of a subsisting license agreement
//   from ARM Limited or its affiliates.
//----------------------------------------------------------------------------

#include <stdlib.h>
#include <stdio.h>
#include "unity_fixture.h"
#include "tiny_cbor_test_runner.h"


static int g_unity_status = EXIT_FAILURE;


int main(int argc, char * argv[])
{
    bool success = 0;
    int rc = 0;


    setvbuf(stdout, (char *)NULL, _IONBF, 0); /* Avoid buffering on test output */
    printf("tiny_cbor_component_tests: Starting component tests...\n");

    printf("----< Test - Start >----\n");
    rc = UnityMain(0, NULL, RunAllTinyCborTests);
    printf("----< Test - End >----\n");


    if (rc > 0) {
        printf("tiny_cbor_component_tests: Test failed.\n");
    }
    else {
        g_unity_status = EXIT_SUCCESS;
        printf("tiny_cbor_component_tests: Test passed.\n");
    }

cleanup:
    // This is detected by test runner app, so that it can know when to terminate without waiting for timeout.
    printf("***END OF TESTS**\n");
    printf("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    printf("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    printf("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

    fflush(stdout);
    return rc;
}
