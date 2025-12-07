"""
SDCRS Unified Process Workflow - Swimlane Diagram
Business Logic: Teacher gets paid only after MC successfully captures/resolves
"""

from processpiper import ProcessMap, EventType, ActivityType, GatewayType

with ProcessMap(
    "SDCRS - Stray Dog Capture & Reporting System",
    colour_theme="GREYWOOF"
) as process_map:

    with process_map.add_pool("SDCRS Process") as pool:

        # Lane 1: Teacher (Reporter)
        with pool.add_lane("Teacher") as teacher_lane:
            start = teacher_lane.add_element("Start", EventType.START)
            submit_app = teacher_lane.add_element("Submit Application\n(Photo+Selfie+GPS)", ActivityType.TASK)
            update_status = teacher_lane.add_element("View Updated\nStatus", ActivityType.TASK)
            receive_payout = teacher_lane.add_element("Receive Payout", ActivityType.TASK)

        # Lane 2: System (Automated)
        with pool.add_lane("System") as system_lane:
            validate_submission = system_lane.add_element("Validate\n(GPS+Boundary+\nTimestamp+Hash)", ActivityType.TASK)
            auto_check = system_lane.add_element("Valid?", GatewayType.EXCLUSIVE)
            auto_reject = system_lane.add_element("Auto Reject", ActivityType.TASK)
            send_notif_sys = system_lane.add_element("Send\nNotification", ActivityType.TASK)
            route_to_queue = system_lane.add_element("Route to\nVerification Queue", ActivityType.TASK)
            route_to_mc = system_lane.add_element("Route to MC\nQueue", ActivityType.TASK)
            award_points = system_lane.add_element("Award Points\n& Process Payout", ActivityType.TASK)

        # Lane 3: Verifier
        with pool.add_lane("Verifier") as verifier_lane:
            review_evidence = verifier_lane.add_element("Review Evidence\n& Compare Duplicates", ActivityType.TASK)
            verify_decision = verifier_lane.add_element("Approve?", GatewayType.EXCLUSIVE)
            approve = verifier_lane.add_element("Approve\nApplication", ActivityType.TASK)
            reject = verifier_lane.add_element("Reject/Duplicate", ActivityType.TASK)
            send_notif_v = verifier_lane.add_element("Send\nNotification", ActivityType.TASK)

        # Lane 4: MC Officer
        with pool.add_lane("MC Officer") as mc_lane:
            view_incidents = mc_lane.add_element("View Verified\nApplications", ActivityType.TASK)
            field_action = mc_lane.add_element("Field Visit\n& Take Action", ActivityType.TASK)
            mc_result = mc_lane.add_element("Success?", GatewayType.EXCLUSIVE)
            captured = mc_lane.add_element("Mark Captured/\nResolved", ActivityType.TASK)
            unable_locate = mc_lane.add_element("Mark Unable\nto Locate", ActivityType.TASK)
            send_notif_mc_ok = mc_lane.add_element("Send\nNotification", ActivityType.TASK)
            send_notif_mc_fail = mc_lane.add_element("Send\nNotification", ActivityType.TASK)

    # Flow connections
    # Teacher submits
    start.connect(submit_app)
    submit_app.connect(validate_submission)

    # System validation
    validate_submission.connect(auto_check)
    auto_check.connect(auto_reject, "No")
    auto_check.connect(route_to_queue, "Yes")
    auto_reject.connect(send_notif_sys)
    send_notif_sys.connect(update_status)

    # Verifier flow
    route_to_queue.connect(review_evidence)
    review_evidence.connect(verify_decision)
    verify_decision.connect(approve, "Yes")
    verify_decision.connect(reject, "No")
    reject.connect(send_notif_v)
    send_notif_v.connect(update_status)

    # Approved -> Route to MC
    approve.connect(route_to_mc)

    # MC Officer flow
    route_to_mc.connect(view_incidents)
    view_incidents.connect(field_action)
    field_action.connect(mc_result)
    mc_result.connect(captured, "Yes")
    mc_result.connect(unable_locate, "No")

    # MC outcomes - Success path (leads to payout)
    captured.connect(award_points)
    award_points.connect(send_notif_mc_ok)
    send_notif_mc_ok.connect(receive_payout)
    receive_payout.connect(update_status)

    # MC outcomes - Failure path (no payout, just notification)
    unable_locate.connect(send_notif_mc_fail)
    send_notif_mc_fail.connect(update_status)

    process_map.draw()
    process_map.save("/Users/__chaks__/Tekdi/djibouti/stray-dog-capture/assets/sdcrs-workflow.png")
    print("Workflow diagram saved")
