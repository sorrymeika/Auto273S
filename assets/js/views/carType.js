define(['$','sl/sl','app','sl/widget/loading'],function (require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        app=require('app'),
        Loading=require('sl/widget/loading');

    module.exports=sl.Activity.extend({
        template: 'views/carType.html',
        events: {
            'tap .js_list li': 'check',
            'tap .js_save': 'save',
            'tap .js_back': 'back'
        },
        onCreate: function () {
            var that=this;

            that.$list=that.$('.js_list');
            that.loading=new Loading(that.$list);

            var userinfo=JSON.parse(localStorage.getItem('USERINFO'));

            that.loading.load({
                url: '/json/carType',
                pageSize: 100,
                data: {
                    auth: userinfo.Auth,
                    account: userinfo.AccountName
                },
                success: function (res) {
                    this.hideLoading();
                    that.$list.html(that.tmpl("list",res));
                },
                error: function (res) {
                    this.hideLoading();
                    sl.tip(res.msg);
                }
            });

        },
        onStart: function () {
        },
        onResume: function () {
        },
        onShow: function () {
            if(!localStorage.getItem('USERINFO')) {
                this.back('/login.html');
            }
        },
        onDestory: function () {
            this.loading&&this.loading.destory();
        },

        check: function (e) {
            var $target=$(e.currentTarget);

            $target.addClass('check').siblings('.check').removeClass('check');
        },

        save: function () {
            var that=this,
                $check=this.$list.find('.check'),
                id=$check.attr('data-id');

            if(!id) {
                sl.tip("请选择车型");
                return;
            }

            that.setResult('carTypeChange',$check.html());
            that.back();

        }

    });
});
