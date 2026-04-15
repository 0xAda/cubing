package rip.ada.groups.wca.patcher;

import rip.ada.groups.wca.OauthSession;
import rip.ada.wcif.Competition;

public record WcifPatchRequest(Competition competition, OauthSession session) {
}
