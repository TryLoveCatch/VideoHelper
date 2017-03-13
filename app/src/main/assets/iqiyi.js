(function() {
        console.log('start');

        try {
            Q.video.auth = function(a, b) {
                var c = $.extend({}, a),
                        d = a.tvid,
                        e = a.vid,
                        f = "http://cache.m.iqiyi.com/jp/tmts/" + d + "/" + e + "/",
                        h = window.weorjjigh ? window.weorjjigh(d) : "";
                c.rate = 2;
                $.extend(c, h);
                var i = 2,
                        j = function() {
                    $.ajax({
                            url:f,
                            dataType:"jsonp",
                            timeout:3e3,
                            cache:!0,
                            data:c,
                            complete:function(a, b) {
                        console.log('complete');
                    },
                    success:
                    function(a) {
                        var json = JSON.stringify(a);
                        console.log('success');
                        javaMethod.getJsonString(json);
                        console.log(f + '?' + $.param(c) + '&callback=jsonp');
                    },
                    error:
                    function(a, b, c) {
                        console.log('error');
                        javaMethod.getJsonString("error");
                    }
                    });
                } ;
                j();
            } ;

            Q.video.load({
                    aid:Q.PageInfo.playInfo.aid,
                    tvid:Q.PageInfo.playInfo.tvid,
                    vid:Q.PageInfo.playInfo.vid,
                    vfrm:Zepto.url.getQueryValue(location.href, 'vfrm'),
                    publicLevel:Q.PageInfo.playInfo.publicLevel,
                    isUGC:Q.PageInfo.playInfo.isUGC,
                    duration:Q.PageInfo.playInfo.duration,
                    ADPlayerID:Q.PageInfo.playInfo.ADPlayerID,
            },{
                complete:
                function() {
                    console.log('complete');
                },
                success:
                function() {
                    console.log('success');
                },
                failure:
                function() {
                    console.log('failure');
                }
            }
            );
            console.log('end');
            console.log(Q.video);
        } catch (err) {
            console.log('error');
            javaMethod.getJsonString("error");
        }
})();