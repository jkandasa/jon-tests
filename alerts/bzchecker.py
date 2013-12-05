import requests
import json
import os
from unittest import SkipTest
import traceback
import logging
from functools import wraps
BUGZILLA='https://bugzilla.redhat.com/'
class blockedBy(object):
    def __init__(self,bz):
        self.bz = bz

    def __call__(self,f):
        @wraps(f)
        def wrap(*args):
            if os.getenv('BZCHECK'):
                json_data = json.dumps([{'ids':self.bz}])
                try:
                    r = requests.get(BUGZILLA+'jsonrpc.cgi?method=Bug.get&params=%s' % json_data)
                except:
                    print 'Could connect to %s not skipping test method' % BUGZILLA
                    traceback.print_exc()
                    f(*args)
                    return
                result = r.json()['result']
                if result and result.has_key('bugs'):
                    for bug in result['bugs']:
                        if bug['status'] in ['NEW','MODIFIED','ASSIGNED','ON_DEV']:
                            bzLink = '%sshow_bug.cgi?id=%s' % (BUGZILLA,bug['id'])
                            msg = 'BZ [%s] %s (%s)' % (bug['status'],bug['summary'],bzLink)
                            raise SkipTest(msg)
            f(*args)
        return wrap

