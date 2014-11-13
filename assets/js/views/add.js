define(['$','sl/sl','app','sl/widget/dropdown','sl/widget/loading'],function (require,exports,module) {
    var $=require('$'),
        sl=require('sl/sl'),
        Dropdown=require('sl/widget/dropdown'),
        Loading=require('sl/widget/loading'),
        app=require('app');

    module.exports=sl.Activity.extend({
        template: 'views/add.html',
        events: {
            'tap .js_save': 'save',
            'tap .js_back': function () {
                var that=this;
                sl.confirm("您的新单信息尚未填充完成，确认后退",function () {
                    that.back();
                });
            },
            'tap [data-upload]': 'upload'
        },
        onCreate: function () {
            var that=this;

            that.photoTypes=[[],[],[],[],[]];
            that.accountId=0;

            that.dropdown=new Dropdown({
                data: [{
                    text: '过户',
                    value: 0
                },{
                    text: '转籍',
                    value: 1
                }],
                isFixed: true,
                attacher: that.$('.js_dropdown'),
                onChange: function (e,i,dataItem) {
                    that.$('.js_dropdown').html(dataItem.text);
                    if(dataItem.value==1) {
                        that.$('.js_region').show();
                        that.$('.js_prize_bd').hide();
                    } else {
                        that.$('.js_region').hide();
                        that.$('.js_prize_bd')[_ACCOUNT_TYPE==1?'hide':'show']();
                    }
                }
            });
            that.$('.js_prize_bd,.js_s')[_ACCOUNT_TYPE==1?'hide':'show']();
            that.$('.js_c')[_ACCOUNT_TYPE==0?'hide':'show']();

            that.listenResult("shopChange",function (e,data) {
                that.$('.js_shop').html(data.shopName);
            });
            that.listenResult("buyerChange",function (e,data) {
                that.$('.js_buyer').html(data.name);
            });
            that.listenResult("sellerChange",function (e,data) {
                that.$('.js_seller').html(data.name);
            });
            that.listenResult('carTypeChange',function (e,data) {
                that.$('.js_car_type').val(data);
            });
            that.listenResult('photoChange',function (e,photoType,src,results) {
                that.$('[data-upload="'+photoType+'"] img').attr('src',src);
                that.photoTypes[photoType-1].splice(0,0,results);
            });
            that.listenResult('sendSelect',function (e,data,name) {
                that.accountId=data;
                that.$('.js_send').val(name);
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
            sl.common.shopInfo=null;
            sl.common.buyerInfo=null;
            sl.common.sellerInfo=null;
            this.loading&&this.loading.destory();
        },

        save: function () {
            var that=this,
                sellerInfo=sl.common.sellerInfo,
                data={
                    plateNumber: that.$('.js_plate_number').val(),
                    price: that.$('.js_price').val(),
                    carType: that.$('.js_car_type').val(),
                    region: that.$('.js_txt_region').val()
                };

            if(!data.plateNumber) {
                sl.tip("请填写车牌号");
                return;
            }
            /*
            if(that.dropdown.index==1&&!data.region) {
            sl.tip("请填写转籍地");
            return;
            }
            */

            if(_ACCOUNT_TYPE==0) {
                /*
                if(!data.carType) {
                sl.tip("请填写车型");
                return;
                }
                if(!sl.common.buyerInfo||!sl.common.buyerInfo.name) {
                sl.tip("请填写买方联系方式");
                return;
                }
                if(!sellerInfo||!sellerInfo.name) {
                sl.tip("请填写卖方联系方式");
                return;
                }
                */

                data.accountId=that.accountId;
                if(sellerInfo) {
                    data.seller=sellerInfo.name||'';
                    data.sellerMobile=sellerInfo.mobile||'';
                    data.sellerAddress=sellerInfo.address||'';
                }
                $.extend(data,sl.common.buyerInfo);
            } else {
                if(!sl.common.shopInfo) {
                    sl.tip("请选择门店");
                    return;
                }
                $.extend(data,sl.common.shopInfo);
            }

            var flag=true,pt;
            $.each(that.photoTypes,function (i,typePhoto) {
                if(typePhoto.length==0) {
                    flag=false;
                    pt=i;
                    //return true;
                } else {
                    data['typePhoto'+(i+1)]=typePhoto.join(',');
                }
            });
            if(flag) {
                //sl.tip("请上传"+["行驶证","保险单","产权证","买方身份证","卖方身份证"][pt]);
                //return;
            }

            var userinfo=JSON.parse(localStorage.getItem('USERINFO'));

            data.auth=userinfo.Auth;
            data.account=userinfo.AccountName;
            data.type=that.dropdown.index;

            !that.loading&&(that.loading=new Loading(that.$el));
            that.loading.load({
                url: '/json/add',
                type: 'POST',
                checkData: false,
                data: data,
                success: function (res) {
                    this.hideLoading();

                    that.setResult('addSuccess');
                    that.back('/');
                },
                error: function (res) {
                    this.hideLoading();
                    sl.tip(res.msg);
                }
            });

        },

        upload: function (e) {
            this.forward('/upload/'+$(e.currentTarget).data('upload')+'.html');
        }

    });
});
