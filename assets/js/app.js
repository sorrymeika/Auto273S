define('app',['$','util'],function(require,exports,module) {

    var $=require('$'),
        util=require('util'),
        ua=navigator.userAgent,
        ios=/iPhone|iPad|iPod/.test(ua),
        isAndroid=/Android/.test(ua),
        slice=Array.prototype.slice,
        blankFn=function() { };

    window.hybirdFunctions={};
    window.complete=function() {
        if(ios&&queue.length!=0) {
            queue.shift();
            if(queue.length!=0) location.href=queue.shift();
        }
    };

    window.trigger=window.app_trigger=function() {
        $.fn.trigger.apply($(window),arguments);
    };

    var queue=[],funcguid=0,
        hybird=function(method,params,hybirdCallback) {

            var data={
                method: method
            },
            hybirdReturn;

            hybirdCallback=typeof params==="function"?params:hybirdCallback;
            params=typeof params==="function"?null:params;

            data.params=params;

            if(typeof hybirdCallback=="function") {
                hybirdReturn="hybirdCallback"+(++funcguid);

                data.callback=hybirdReturn;
                hybirdFunctions[hybirdReturn]=function() {
                    hybirdCallback.apply(null,arguments);
                    delete hybirdFunctions[hybirdReturn];
                };
            }

            if(bridge.isDevelopment) {
                switch(data.method) {
                    case 'exitLauncher':
                        hybirdFunctions[hybirdReturn]();
                        break;
                }
                return;
            }

            if(ios) {
                var url='execslhybirdjsa:a?'+encodeURIComponent(JSON.stringify(data));
                queue.push(url);
                if(queue.length==1) {
                    location.href=url;
                }
            } else if(isAndroid) {
                prompt(JSON.stringify(data));
            }
        },
        bridge={
            isAndroid: isAndroid,
            versionName: isAndroid?'1.0':"1.0",
            exec: hybird,
            exitLauncher: function(f) {
                hybird('exitLauncher',function() {
                    f&&f();
                });
            },
            tip: function(msg) {
                hybird('tip',msg+"");
            },
            pickImage: function(f) {
                hybird('pickImage',f);
            },
            takePhoto: function(f) {
                hybird('takePhoto',f);
            },
            queryThumbnailList: function(f) {
                hybird('queryThumbnailList',f);
            },
            pickColor: function(f) {
                hybird('pickColor',f);
            },
            share: function() {
                hybird('share');
            },
            isDevelopment: navigator.platform=="Win32"||navigator.platform=="Win64",
            url: function(url) {
                return /^http\:\/\//.test(url)?url:navigator.platform=="Win32"||navigator.platform=="Win64"?url:('http://273.ie1e.com'+url);
            },
            post: function(url,data,files,callback) {
                callback=typeof files==='function'?files:callback;
                files=typeof files==='function'?null:files;

                hybird('post',{
                    url: this.url(url),
                    files: files,
                    data: data
                },callback);
            },
            exit: function() {
                hybird('exit');
            },
            update: function(downloadUrl,versionName,f) {
                hybird('updateApp',{
                    downloadUrl: downloadUrl,
                    versionName: versionName
                },f);
            }
        };

    return bridge;

});
