package com.sigmob.sdk.base.common;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.AndroidMarket;
import com.sigmob.sdk.videoAd.InterActionType;
import com.sigmob.windad.WindAds;

import java.util.EnumSet;

public class UrlHandler {

    private static final ResultActions EMPTY_CLICK_LISTENER = new ResultActions() {

        @Override
        public void urlHandlingSucceeded(String url, UrlAction urlAction) {
        }

        @Override
        public void urlHandlingFailed(String url, UrlAction lastFailedUrlAction) {
        }
    };
    private static final SigmobSchemeListener EMPTY_SIGMOB_SCHEME_LISTENER =
            new SigmobSchemeListener() {
                @Override
                public void onFinishLoad() {
                }

                @Override
                public void onClose() {
                }

                @Override
                public void onFailLoad() {
                }
            };
    private final EnumSet<UrlAction> mSupportedUrlActions;
    private final ResultActions mResultActions, mDeeplinkresultActions;
    private final SigmobSchemeListener mSigmobSchemeListener;
    private final BaseAdUnit mAdUnit;
    private final boolean mSkipShowSigmobBrowser;
    private boolean mAlreadySucceeded;
    private boolean mTaskPending;
    private boolean mWithoOutresolvedUrl;

    /**
     * Do not instantiate UrlHandler directly; use {@link Builder} instead.
     */
    private UrlHandler(
            final EnumSet<UrlAction> supportedUrlActions,
            final ResultActions resultActions,
            final ResultActions deepLinkResultActions,

            final SigmobSchemeListener sigmobSchemeListener,
            final boolean skipShowSigmobBrowser,
            final BaseAdUnit adUnit,
            final boolean withoOutresolvedUrl) {
        mSupportedUrlActions = EnumSet.copyOf(supportedUrlActions);
        mResultActions = resultActions;
        mDeeplinkresultActions = deepLinkResultActions;
        mSigmobSchemeListener = sigmobSchemeListener;
        mSkipShowSigmobBrowser = skipShowSigmobBrowser;
        mAdUnit = adUnit;
        mAlreadySucceeded = false;
        mTaskPending = false;
        mWithoOutresolvedUrl = withoOutresolvedUrl;
    }

    SigmobSchemeListener getSigmobSchemeListener() {
        return mSigmobSchemeListener;
    }

    boolean shouldSkipShowSigmobBrowser() {
        return mSkipShowSigmobBrowser;
    }


//    /**
//     * Performs the actual click handling by verifying that the {@code destinationUrl} is one of
//     * the configured supported {@link UrlAction}s and then handling it accordingly.
//     *
//     * @param context        The activity context.
//     * @param destinationUrl The URL to handle.
//     */
//    public void handleUrl(final Context context, final String destinationUrl) {
//        Preconditions.NoThrow.checkNotNull(context);
//
//        handleUrl(context, destinationUrl, true);
//    }




    /**
     * Follows any redirects from {@code destinationUrl} and then handles the URL accordingly.
     *
     * @param context             The activity context.
     */
    public void handleUrl(final Context context,String url) {
        Preconditions.NoThrow.checkNotNull(context);

        handleResolvedUrl(context,url);
    }



    /**
     * Performs the actual url handling by verifying that the {@code destinationUrl} is one of
     * the configured supported {@link UrlAction}s and then handling it accordingly.
     *
     * @param context             The activity context.
     * @return true if the given URL was successfully handled; false otherwise
     */
    private boolean handleResolvedUrl(final Context context,String url) {


        UrlAction lastFailedUrlAction = UrlAction.NOOP;

        String lastUrl = null;
        for (final UrlAction followDeepLink : mSupportedUrlActions) {

            String handleUrL = followDeepLink.getHandleUrL(mAdUnit);


            if (!TextUtils.isEmpty(url) && mAdUnit.getInteractionType() != InterActionType.MiniProgramType){
                handleUrL = url;
            }

            if (!TextUtils.isEmpty(handleUrL)){
                handleUrL = mAdUnit.getMacroCommon().macroProcess(handleUrL);
                lastUrl = handleUrL;
                try {
                    final Uri deeplinkUri = Uri.parse(handleUrL);
                    AndroidMarket androidMarket = mAdUnit.getAndroidMarket();

                    if (followDeepLink == UrlAction.FOLLOW_DEEP_LINK){
                        if (deeplinkUri.getScheme().equalsIgnoreCase("market")
                                && androidMarket != null && !TextUtils.isEmpty(androidMarket.market_url)){
                            continue;
                        }
                    }


                    final String finalHandleUrL = handleUrL;

                    if (followDeepLink.shouldTryHandlingUri(deeplinkUri, mAdUnit.getInteractionType())){

                        if (!mWithoOutresolvedUrl && UrlAction.OPEN_WITH_BROWSER == followDeepLink){

                            final UrlHandler finalUrlHandler = this;
                            UrlResolutionTask.getResolvedUrl(finalHandleUrL, new UrlResolutionTask.UrlResolutionListener() {
                                    @Override
                                    public void onSuccess(String resolvedUrl) {


                                        UrlAction urlAction = followDeepLink;
                                        if (!resolvedUrl.toLowerCase().startsWith("http")){
                                            urlAction = UrlAction.FOLLOW_DEEP_LINK;
                                        }
                                        final Uri uri = Uri.parse(resolvedUrl);
                                        final UrlAction finalUrlAction = urlAction;

                                        try {
                                            urlAction.performAction(context,uri,finalUrlHandler,mAdUnit);
                                            WindAds.sharedAds().getHandler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        mResultActions.urlHandlingSucceeded(resolvedUrl, finalUrlAction);
                                                    }catch (Throwable th){

                                                    }
                                                }
                                            });
                                        } catch (Exception e) {
                                            WindAds.sharedAds().getHandler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        mResultActions.urlHandlingFailed(resolvedUrl,finalUrlAction);

                                                    }catch (Throwable th){

                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(String message, Throwable throwable) {
                                        try {
                                            followDeepLink.performAction(context,deeplinkUri,finalUrlHandler,mAdUnit);
                                            WindAds.sharedAds().getHandler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        mResultActions.urlHandlingSucceeded(finalHandleUrL,followDeepLink);

                                                    }catch (Throwable t){

                                                    }
                                                }
                                            });
                                        } catch (Exception e) {
                                            WindAds.sharedAds().getHandler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        mResultActions.urlHandlingFailed(finalHandleUrL,followDeepLink);
                                                    }catch (Throwable t){

                                                    }
                                                }
                                            });
                                        }


                                    }
                                });

                        }else {
                            followDeepLink.performAction(context,deeplinkUri,this,mAdUnit);

                            WindAds.sharedAds().getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    mResultActions.urlHandlingSucceeded(finalHandleUrL,followDeepLink);
                                }
                            });
                        }

                        return true;
                    }
                }catch (Throwable th){
                    mResultActions.urlHandlingFailed(handleUrL,followDeepLink);
                }
            }
        }
        try {
            failUrlHandling(lastUrl, lastFailedUrlAction, "Link ignored. Unable to handle url: " , null);

        } catch (Throwable throwable) {
            SigmobLog.e("handleResolvedUrl eroor", throwable);
        }
        return false;
    }

    private void failUrlHandling(final String url, UrlAction urlAction,
                                 final String message, final Throwable throwable) {
        Preconditions.NoThrow.checkNotNull(message);

        if (urlAction == null) {
            urlAction = UrlAction.NOOP;
        }

        SigmobLog.d(message, throwable);

        mResultActions.urlHandlingFailed(url, urlAction);
    }

    /**
     * {@code ClickListener} defines the methods that {@link UrlHandler} calls when handling a
     * certain click succeeds or fails.
     */
    public interface ResultActions {
        /**
         * Called if the URL matched a supported {@link UrlAction} and was resolvable. Will be
         * called at most 1 times and is mutually exclusive with
         * {@link ResultActions#urlHandlingFailed(String, UrlAction)}.
         */
        void urlHandlingSucceeded(final String url, final UrlAction urlAction);

        /**
         * Called with {@link UrlAction#NOOP} if the URL did not match any supported
         * {@link UrlAction}s; or, called with the last matching {@link UrlAction} if URL was
         * unresolvable. Will be called at most 1 times and is mutually exclusive with
         * {@link ResultActions#urlHandlingSucceeded(String, UrlAction)}.
         */
        void urlHandlingFailed(final String url,
                               final UrlAction lastFailedUrlAction);
    }

    /**
     * {@code SigmobSchemeListener} defines the methods that {@link UrlHandler} calls when handling
     * {@code HANDLE_SIGMOB_SCHEME} URLs.
     */
    public interface SigmobSchemeListener {
        void onFinishLoad();

        void onClose();

        void onFailLoad();
    }

    /**
     * {@code Builder} provides an API to configure an immutable {@link UrlHandler} and create it.
     */
    public static class Builder {

        private EnumSet<UrlAction> supportedUrlActions = EnumSet.of(UrlAction.NOOP);

        private ResultActions resultActions = EMPTY_CLICK_LISTENER;
        private ResultActions deeplinkresultActions = EMPTY_CLICK_LISTENER;

        private SigmobSchemeListener sigmobSchemeListener = EMPTY_SIGMOB_SCHEME_LISTENER;
        private boolean skipShowSigmobBrowser = false;
        private boolean withoOutresolvedUrl = false;
        private BaseAdUnit mAdUnit;

        /**
         * Sets the {@link UrlAction}s to support in the {@code UrlHandler} to build.
         *
         * @param first  A {@code UrlAction} for the {@code UrlHandler} to support.
         * @param others An arbitrary number of {@code UrlAction}s for the {@code UrlHandler} to
         *               support.
         * @return A {@link Builder} with the desired supported {@code UrlAction}s added.
         */
        public Builder withSupportedUrlActions(final UrlAction first,
                                               final UrlAction... others) {
            this.supportedUrlActions = EnumSet.of(first, others);
            return this;
        }

        /**
         * Sets the {@link ResultActions} for the {@code UrlHandler} to
         * build.
         *
         * @param resultActions A {@code ClickListener} for the {@code UrlHandler}.
         * @return A {@link Builder} with the desired {@code ClickListener} added.
         */
        public Builder withResultActions(final ResultActions resultActions) {
            this.resultActions = resultActions;
            return this;
        }



        /**
         * Sets the {@link ResultActions} for the {@code UrlHandler} to
         * build.
         *
         * @param resultActions A {@code ClickListener} for the {@code UrlHandler}.
         * @return A {@link Builder} with the desired {@code ClickListener} added.
         */
        public Builder withDeeplinkResultActions(final ResultActions resultActions) {
            this.deeplinkresultActions = resultActions;
            return this;
        }


        /**
         * Sets the {@link SigmobSchemeListener} for the {@code UrlHandler} to build.
         *
         * @param sigmobSchemeListener A {@code SigmobSchemeListener} for the {@code UrlHandler}.
         * @return A {@link Builder} with the desired {@code SigmobSchemeListener} added.
         */
        public Builder withSigmobSchemeListener(
                final SigmobSchemeListener sigmobSchemeListener) {
            this.sigmobSchemeListener = sigmobSchemeListener;
            return this;
        }

        /**
         * @return A {@link Builder} that will skip starting a {@code SigmobBrowser}.
         */
        public Builder withoutSigmobBrowser(boolean skip) {
            this.skipShowSigmobBrowser = skip;
            return this;
        }

        /**
         * @return A {@link Builder} that will skip starting a {@code SigmobBrowser}.
         */
        public Builder withoutResolvedUrl(boolean enable) {
            this.withoOutresolvedUrl = enable;
            return this;
        }

        /**
         * Sets the broadcastIdentifier for the ad associated with this URL
         *
         * @return A {@link Builder} that knows the broadcastIdentifier for the ad.
         */
        public Builder withAdunit(final BaseAdUnit adunit) {
            mAdUnit = adunit;
            return this;
        }

        /**
         * Creates an immutable {@link UrlHandler} with the desired configuration, according to the
         * other {@link Builder} methods called before.
         *
         * @return An immutable {@code UrlHandler} with the desired configuration.
         */
        public UrlHandler build() {
            return new UrlHandler(supportedUrlActions, resultActions, deeplinkresultActions, sigmobSchemeListener,
                    skipShowSigmobBrowser, mAdUnit, withoOutresolvedUrl);
        }
    }

}

