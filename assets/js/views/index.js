define('views/index',['zepto','sl/sl','app','sl/widget/loading','sl/widget/dialog'],function (require,exports,module) {
    var $=require('zepto'),
        sl=require('sl/sl'),
        Loading=require('sl/widget/loading'),
        Dialog=require('sl/widget/dialog'),
        app=require('app');

    module.exports=sl.Activity.extend({
        template: 'views/index.html',
        events: {
            'tap .js_list li': 'select',
            'tap .transfer_tab li': function (e) {
                this._type=$(e.currentTarget).index();
                $(e.currentTarget).addClass('curr').siblings('.curr').removeClass('curr');
                this.$list.html("");
                this._keywords='';
                this.load();
            },
            'tap .js_send': function () {
                var $target=this.$('.js_list li.edit');
                this.forward("/send/"+$target.data('shopid')+'/'+$target.data('id')+".html")
            },
            'tap .js_finish': function () {
                var that=this,
                    $target=this.$('.js_list li.edit');
                if($target.data('persent')!='100%') {
                    sl.tip("信息不全，无法执行结单操作");
                    return;
                }
                sl.confirm("确认结单？",function () {
                    var userinfo=JSON.parse(localStorage.getItem('USERINFO'));

                    that.loading.load({
                        url: '/json/finish',
                        checkData: false,
                        data: {
                            id: $target.data('id'),
                            auth: userinfo.Auth,
                            account: userinfo.AccountName
                        },
                        success: function (res) {
                            this.hideLoading();
                            sl.tip("结单成功");

                            that.load();
                        },
                        error: function (res) {
                            this.hideLoading();
                            sl.tip(res.msg);
                        }
                    });
                });

            },
            'tap .js_edit': function () {
                var $target=this.$('.js_list li.edit');
                this.forward("/modify/"+$target.data('type')+'/'+$target.data('id')+".html")
            },
            'tap .js_del': function () {
                var that=this,
                    $target=this.$('.js_list li.edit');

                sl.confirm("确认删除？",function () {
                    var userinfo=JSON.parse(localStorage.getItem('USERINFO'));

                    that.loading.load({
                        url: '/json/del',
                        checkData: false,
                        data: {
                            id: $target.data('id'),
                            auth: userinfo.Auth,
                            account: userinfo.AccountName
                        },
                        success: function (res) {
                            this.hideLoading();
                            sl.tip("删除成功");

                            that.load();
                        },
                        error: function (res) {
                            this.hideLoading();
                            sl.tip(res.msg);
                        }
                    });

                });
            }
        },
        onCreate: function () {
            var that=this;

            that.listenResult('addSuccess',function () {
                that.isDataLoad=false;
            });
            that.listenResult('sendSuccess',function () {
                that.isDataLoad=false;
            });
            that.listenResult('searchChange',function (e,keywords) {
                that._keywords=keywords;
                that.isDataLoad=false;
            });

            that.$list=that.$('.js_list');
            that.loading=new Loading(that.$list);
            that.$edit=$('<div class="list_edit">'+(_ACCOUNT_TYPE=="1"?'<b class="finish js_finish">结单</b>':'<b class="send js_send">派单</b>')+'<b class="edit js_edit">编辑</b><b class="del js_del">删除</b></div>');
        },
        onStart: function () {
        },
        onResume: function () {
        },
        onShow: function () {
            var that=this;

            if(!localStorage.getItem('USERINFO')) {
                that.redirect('/login.html');
            } else if(!that.isDataLoad) {
                that.load();
            }
        },

        _type: 0,
        _keywords: '',

        load: function () {
            var that=this;
            var userinfo=JSON.parse(localStorage.getItem('USERINFO'));
            that.$list.html("");
            that.loading.load({
                url: '/json/transfer?action=list',
                data: {
                    auth: userinfo.Auth,
                    account: userinfo.AccountName,
                    type: that._type,
                    keywords: that._keywords
                },
                success: function (res) {
                    this.hideLoading();
                    that.isDataLoad=true;
                    that.$list.html(that.tmpl('list',res));
                },
                refresh: function (res) {
                    that.$list.append(that.tmpl('list',res));
                },
                error: function (res) {
                    this.hideLoading();
                    sl.tip(res.msg);
                }
            });
        },

        onDestory: function () {
            console.log("index onDestory");
        },

        select: function (e) {
            var that=this,
                $target=$(e.currentTarget);

            $target.siblings('.edit').removeClass('edit');
            $target.toggleClass('edit').hasClass('edit')?$target.after(that.$edit.show()):that.$edit.hide();
        }
    });
});
