package rip.ada.wca.patcher;

import rip.ada.wca.OauthSession;
import rip.ada.wcif.Competition;

public record WcifPatchRequest(Competition competition, OauthSession session) {
}
