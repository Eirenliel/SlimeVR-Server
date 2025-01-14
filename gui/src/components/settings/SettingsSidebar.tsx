import classNames from 'classnames';
import { ReactNode, useMemo } from 'react';
import { NavLink, useLocation, useMatch } from 'react-router-dom';
import { Typography } from '@/components/commons/Typography';
import { useLocalization } from '@fluent/react';

export function SettingsLink({
  to,
  scrollTo,
  children,
}: {
  to: string;
  scrollTo?: string;
  children: ReactNode;
}) {
  const { state } = useLocation();
  const doesMatch = useMatch({
    path: to,
  });

  const isActive = useMemo(() => {
    const typedState: { scrollTo?: string } = state as any;
    return (
      (doesMatch && !scrollTo && !typedState?.scrollTo) ||
      (doesMatch && typedState?.scrollTo == scrollTo)
    );
  }, [state, doesMatch]);

  return (
    <NavLink
      to={to}
      state={{ scrollTo }}
      className={classNames('pl-5 py-2 hover:bg-background-60 rounded-lg', {
        'bg-background-60': isActive,
      })}
    >
      {children}
    </NavLink>
  );
}

export function SettingsSidebar() {
  const { l10n } = useLocalization();

  return (
    <div className="flex flex-col px-5 py-5 gap-3 overflow-y-auto bg-background-70 rounded-lg h-full">
      <Typography variant="main-title">
        {l10n.getString('settings-sidebar-title')}
      </Typography>
      <div className="flex flex-col gap-3">
        <Typography variant="section-title">
          {l10n.getString('settings-sidebar-general')}
        </Typography>
        <div className="flex flex-col gap-2">
          <SettingsLink to="/settings/trackers" scrollTo="steamvr">
            SteamVR
          </SettingsLink>
          <SettingsLink to="/settings/trackers" scrollTo="mechanics">
            {l10n.getString('settings-sidebar-tracker_mechanics')}
          </SettingsLink>
          <SettingsLink to="/settings/trackers" scrollTo="fksettings">
            {l10n.getString('settings-sidebar-fk_settings')}
          </SettingsLink>
          <SettingsLink to="/settings/trackers" scrollTo="gestureControl">
            {l10n.getString('settings-sidebar-gesture_control')}
          </SettingsLink>
        </div>
      </div>
      <div className="flex flex-col gap-3">
        <Typography variant="section-title">
          {l10n.getString('settings-sidebar-interface')}
        </Typography>
        <div className="flex flex-col gap-2">
          <SettingsLink to="/settings/interface" scrollTo="notifications">
            {l10n.getString('settings-sidebar-notifications')}
          </SettingsLink>
          <SettingsLink to="/settings/interface" scrollTo="behavior">
            {l10n.getString('settings-sidebar-behavior')}
          </SettingsLink>
          <SettingsLink to="/settings/interface" scrollTo="appearance">
            {l10n.getString('settings-sidebar-appearance')}
          </SettingsLink>
        </div>
        <div className="flex flex-col gap-3">
          <Typography variant="section-title">OSC</Typography>
          <div className="flex flex-col gap-2">
            <SettingsLink to="/settings/osc/router" scrollTo="router">
              {l10n.getString('settings-sidebar-osc_router')}
            </SettingsLink>
            <SettingsLink to="/settings/osc/vrchat" scrollTo="vrchat">
              {l10n.getString('settings-sidebar-osc_trackers')}
            </SettingsLink>
            <SettingsLink to="/settings/osc/vmc" scrollTo="vmc">
              VMC
            </SettingsLink>
          </div>
        </div>
        <div className="flex flex-col gap-3">
          <Typography variant="section-title">
            {l10n.getString('settings-sidebar-utils')}
          </Typography>
          <div className="flex flex-col gap-2">
            <SettingsLink to="/settings/serial">
              {l10n.getString('settings-sidebar-serial')}
            </SettingsLink>
            <SettingsLink to="/settings/firmware-tool">
              {l10n.getString('settings-sidebar-firmware-tool')}
            </SettingsLink>
          </div>
          <div className="flex flex-col gap-2">
            <SettingsLink to="/settings/advanced">
              {l10n.getString('settings-sidebar-advanced')}
            </SettingsLink>
          </div>
        </div>
      </div>
    </div>
  );
}
