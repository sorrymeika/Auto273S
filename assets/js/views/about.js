define(['$','sl/sl','app'],function(require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        app=require('app'),
        util=require('util');

    module.exports=sl.Activity.extend({
        template: 'views/about.html',
        events: {},
        onCreate: function() {
            var that=this;

            that.$('.js_type').html(_ACCOUNT_TYPE=="0"?"商务端 V 1.0版":"业务端 V 1.0版");
        },
        onStart: function() {
        },
        onResume: function() {
        },
        onDestory: function() {
        }
    });;
});